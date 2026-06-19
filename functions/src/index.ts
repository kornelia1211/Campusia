import { setGlobalOptions } from "firebase-functions";
import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import {
    getFirestore,
    FieldValue,
    Timestamp,
} from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { initializeApp } from "firebase-admin/app";
import * as logger from "firebase-functions/logger";

initializeApp();

setGlobalOptions({
    maxInstances: 10,
});

const db = getFirestore();

const DAY_MILLIS = 24 * 60 * 60 * 1000;

function chunkArray<T>(
    items: T[],
    chunkSize: number
): T[][] {
    const chunks: T[][] = [];

    for (let index = 0; index < items.length; index += chunkSize) {
        chunks.push(items.slice(index, index + chunkSize));
    }

    return chunks;
}

async function getCourseStudentTokens(
    courseId: string
): Promise<string[]> {
    const courseSnapshot = await db
        .collection("courses")
        .doc(courseId)
        .get();

    if (!courseSnapshot.exists) {
        logger.warn("Course does not exist", {
            courseId,
        });

        return [];
    }

    const studentIds =
        (courseSnapshot.get("studentIds") as string[] | undefined) ?? [];

    logger.info("Loaded course studentIds", {
        courseId,
        studentIdsCount: studentIds.length,
        studentIds,
    });

    if (studentIds.length === 0) {
        return [];
    }

    const tokens = new Set<string>();

    // Wariant 1: studentIds są ID dokumentów users/{uid}
    const userSnapshotsByDocId = await Promise.all(
        studentIds.map((studentId) =>
            db.collection("users").doc(studentId).get()
        )
    );

    userSnapshotsByDocId.forEach((userSnapshot) => {
        const userTokens =
            userSnapshot.get("fcmTokens") as string[] | undefined;

        (userTokens ?? []).forEach((token) => {
            tokens.add(token);
        });
    });

    // Wariant 2: studentIds są zapisane w polu userId
    // Firestore "in" obsługuje ograniczoną liczbę elementów, więc robimy chunki.
    const chunks = chunkArray(studentIds, 10);

    for (const chunk of chunks) {
        const usersByUserIdSnapshot = await db
            .collection("users")
            .where("userId", "in", chunk)
            .get();

        usersByUserIdSnapshot.docs.forEach((userDocument) => {
            const userTokens =
                userDocument.get("fcmTokens") as string[] | undefined;

            (userTokens ?? []).forEach((token) => {
                tokens.add(token);
            });
        });
    }

    logger.info("Loaded FCM tokens", {
        courseId,
        tokenCount: tokens.size,
    });

    return Array.from(tokens);
}


async function getStudentTokens(
    studentId: string
): Promise<string[]> {
    const tokens = new Set<string>();

    const userByDocId = await db
        .collection("users")
        .doc(studentId)
        .get();

    if (userByDocId.exists) {
        const role =
            (userByDocId.get("role") as string | undefined)
                ?.trim()
                .toLowerCase();

        if (role === "student") {
            const userTokens =
                userByDocId.get("fcmTokens") as string[] | undefined;

            (userTokens ?? []).forEach((token) => {
                tokens.add(token);
            });
        }
    }

    const usersByUserIdSnapshot = await db
        .collection("users")
        .where("userId", "==", studentId)
        .get();

    usersByUserIdSnapshot.docs.forEach((userDocument) => {
        const role =
            (userDocument.get("role") as string | undefined)
                ?.trim()
                .toLowerCase();

        if (role !== "student") {
            return;
        }

        const userTokens =
            userDocument.get("fcmTokens") as string[] | undefined;

        (userTokens ?? []).forEach((token) => {
            tokens.add(token);
        });
    });

    logger.info("Loaded grade notification student tokens", {
        studentId,
        tokenCount: tokens.size,
    });

    return Array.from(tokens);
}

async function sendGradeNotification(
    assignmentId: string,
    studentId: string,
    assignmentTitle: string,
    gradePercent: number
): Promise<void> {
    const tokens = await getStudentTokens(studentId);

    if (tokens.length === 0) {
        logger.warn("No FCM tokens found for grade notification", {
            assignmentId,
            studentId,
        });

        return;
    }

    logger.info("Sending grade notification", {
        assignmentId,
        studentId,
        tokenCount: tokens.length,
        gradePercent,
    });

    const tokenChunks = chunkArray(tokens, 500);
    const invalidTokens: string[] = [];

    for (const tokenChunk of tokenChunks) {
        const response = await getMessaging().sendEachForMulticast({
            tokens: tokenChunk,
            data: {
                type: "assignment_grade",
                assignmentId,
                title: "Assignment graded",
                body: `Your assignment "${assignmentTitle}" was graded: ${gradePercent}%`,
            },
            android: {
                priority: "high",
            },
        });

        response.responses.forEach((result, index) => {
            if (!result.success) {
                logger.error("FCM grade send error", {
                    assignmentId,
                    studentId,
                    errorCode: result.error?.code,
                    errorMessage: result.error?.message,
                });

                const errorCode = result.error?.code;

                if (
                    errorCode ===
                        "messaging/registration-token-not-registered" ||
                    errorCode ===
                        "messaging/invalid-registration-token"
                ) {
                    invalidTokens.push(tokenChunk[index]);
                }
            }
        });

        logger.info("FCM grade multicast result", {
            assignmentId,
            studentId,
            successCount: response.successCount,
            failureCount: response.failureCount,
        });
    }

    await removeInvalidTokens(invalidTokens);
}

async function removeInvalidTokens(
    invalidTokens: string[]
): Promise<void> {
    if (invalidTokens.length === 0) {
        return;
    }

    const usersSnapshot = await db.collection("users").get();
    const batch = db.batch();

    usersSnapshot.docs.forEach((userDocument) => {
        batch.update(userDocument.ref, {
            fcmTokens: FieldValue.arrayRemove(...invalidTokens),
        });
    });

    await batch.commit();
}

async function sendAssignmentReminder(
    assignmentId: string,
    courseId: string,
    title: string,
    dueDate: Timestamp
): Promise<void> {
    const tokens = await getCourseStudentTokens(courseId);

    if (tokens.length === 0) {
        logger.warn("No FCM tokens found for assignment reminder", {
            assignmentId,
            courseId,
        });

        return;
    }

    const dueDateText = dueDate
        .toDate()
        .toLocaleString("en-GB", {
            timeZone: "Europe/Warsaw",
        });

    logger.info("Sending assignment reminder", {
        assignmentId,
        courseId,
        tokenCount: tokens.length,
    });

    const tokenChunks = chunkArray(tokens, 500);
    const invalidTokens: string[] = [];

    for (const tokenChunk of tokenChunks) {
        const response = await getMessaging().sendEachForMulticast({
            tokens: tokenChunk,
            data: {
                type: "assignment_deadline",
                assignmentId,
                courseId,
                title: "Assignment deadline approaching",
                body: `"${title}" is due ${dueDateText}`,
            },
            android: {
                priority: "high",
            },
        });

        response.responses.forEach((result, index) => {
            if (!result.success) {
                logger.error("FCM send error", {
                    assignmentId,
                    errorCode: result.error?.code,
                    errorMessage: result.error?.message,
                });

                const errorCode = result.error?.code;

                if (
                    errorCode ===
                        "messaging/registration-token-not-registered" ||
                    errorCode ===
                        "messaging/invalid-registration-token"
                ) {
                    invalidTokens.push(tokenChunk[index]);
                }
            }
        });

        logger.info("FCM multicast result", {
            assignmentId,
            successCount: response.successCount,
            failureCount: response.failureCount,
        });
    }

    await removeInvalidTokens(invalidTokens);
}

async function sendAnnouncementNotification(
    announcementId: string,
    courseId: string,
    announcementTitle: string,
    announcementMessage: string
): Promise<void> {
    const tokens = await getCourseStudentTokens(courseId);

    if (tokens.length === 0) {
        logger.warn("No FCM tokens found for announcement notification", {
            announcementId,
            courseId,
        });

        return;
    }

    const trimmedMessage = announcementMessage.trim();
    const shortMessage =
        trimmedMessage.length > 120
            ? `${trimmedMessage.substring(0, 117)}...`
            : trimmedMessage;

    logger.info("Sending announcement notification", {
        announcementId,
        courseId,
        tokenCount: tokens.length,
    });

    const tokenChunks = chunkArray(tokens, 500);
    const invalidTokens: string[] = [];

    for (const tokenChunk of tokenChunks) {
        const response = await getMessaging().sendEachForMulticast({
            tokens: tokenChunk,
            data: {
                type: "course_announcement",
                announcementId,
                courseId,
                title: "New course announcement",
                body: `${announcementTitle}: ${shortMessage}`,
            },
            android: {
                priority: "high",
            },
        });

        response.responses.forEach((result, index) => {
            if (!result.success) {
                logger.error("FCM announcement send error", {
                    announcementId,
                    courseId,
                    errorCode: result.error?.code,
                    errorMessage: result.error?.message,
                });

                const errorCode = result.error?.code;

                if (
                    errorCode ===
                        "messaging/registration-token-not-registered" ||
                    errorCode ===
                        "messaging/invalid-registration-token"
                ) {
                    invalidTokens.push(tokenChunk[index]);
                }
            }
        });

        logger.info("FCM announcement multicast result", {
            announcementId,
            courseId,
            successCount: response.successCount,
            failureCount: response.failureCount,
        });
    }

    await removeInvalidTokens(invalidTokens);
}

export const onAssignmentCreated = onDocumentCreated(
    "assignments/{assignmentId}",
    async (event) => {
        const snapshot = event.data;

        if (!snapshot) {
            return;
        }

        const assignment = snapshot.data();

        const assignmentId = snapshot.id;
        const courseId = assignment.courseId as string | undefined;
        const title = assignment.title as string | undefined;
        const dueDate = assignment.dueDate as Timestamp | undefined;

        logger.info("Assignment created trigger started", {
            assignmentId,
            courseId,
            title,
            hasDueDate: dueDate != null,
        });

        if (!courseId || !title || !dueDate) {
            logger.warn("Assignment notification skipped: missing data", {
                assignmentId,
                courseId,
                title,
                hasDueDate: dueDate != null,
            });

            return;
        }

        const now = Date.now();
        const dueTime = dueDate.toMillis();
        const timeUntilDeadline = dueTime - now;

        if (timeUntilDeadline <= 0) {
            logger.info("Assignment notification skipped: deadline passed", {
                assignmentId,
            });

            return;
        }

        const isShortDeadline =
            timeUntilDeadline <= DAY_MILLIS;

        const jobId =
            isShortDeadline
                ? `${assignmentId}_created`
                : `${assignmentId}_deadline`;

        const sendAt =
            isShortDeadline
                ? Timestamp.fromMillis(
                    now
                )
                : Timestamp.fromMillis(
                    dueTime - DAY_MILLIS
                );

        await db.collection("assignment_notification_jobs")
            .doc(jobId)
            .set({
                assignmentId,
                courseId,
                title,
                dueDate,
                sendAt,
                sent: false,
                type:
                    isShortDeadline
                        ? "short_deadline"
                        : "day_before",
                createdAt: FieldValue.serverTimestamp(),
            });


        logger.info("TEST: assignment created trigger fired");
        logger.info("Assignment notification job created", {
            assignmentId,
            jobId,
            sendAtMillis: sendAt.toMillis(),
            isShortDeadline,
        });
    }
);


export const onAnnouncementCreated = onDocumentCreated(
    "announcements/{announcementId}",
    async (event) => {
        const snapshot = event.data;

        if (!snapshot) {
            return;
        }

        const announcement = snapshot.data();

        const announcementId = snapshot.id;
        const courseId = announcement.courseId as string | undefined;
        const title = announcement.title as string | undefined;
        const message = announcement.message as string | undefined;

        logger.info("Announcement created trigger started", {
            announcementId,
            courseId,
            title,
            hasMessage: message != null,
        });

        if (!courseId || !title || !message) {
            logger.warn("Announcement notification skipped: missing data", {
                announcementId,
                courseId,
                title,
                hasMessage: message != null,
            });

            return;
        }

        const jobId = `${announcementId}_announcement`;

        await db.collection("assignment_notification_jobs")
            .doc(jobId)
            .set({
                announcementId,
                courseId,
                title,
                message,
                sendAt: Timestamp.fromMillis(
                    Date.now()
                ),
                sent: false,
                type: "announcement_created",
                createdAt: FieldValue.serverTimestamp(),
            });

        logger.info("Announcement notification job created", {
            announcementId,
            courseId,
            jobId,
        });
    }
);

export const onSubmissionGradeUpdated = onDocumentUpdated(
    "assignments/{assignmentId}/submissions/{studentId}",
    async (event) => {
        const before = event.data?.before.data();
        const after = event.data?.after.data();

        if (!before || !after) {
            return;
        }

        const assignmentId = event.params.assignmentId;
        const studentId = event.params.studentId;

        const beforeGrade = before.gradePercent as number | undefined;
        const afterGrade = after.gradePercent as number | undefined;

        if (afterGrade == null) {
            logger.info("Grade notification skipped: grade missing", {
                assignmentId,
                studentId,
            });

            return;
        }

        if (beforeGrade === afterGrade) {
            logger.info("Grade notification skipped: grade unchanged", {
                assignmentId,
                studentId,
                gradePercent: afterGrade,
            });

            return;
        }

        const assignmentSnapshot = await db
            .collection("assignments")
            .doc(assignmentId)
            .get();

        const assignmentTitle =
            (assignmentSnapshot.get("title") as string | undefined) ??
            "Assignment";

        const jobId = `${assignmentId}_${studentId}_grade_${Date.now()}`;

        await db.collection("assignment_notification_jobs")
            .doc(jobId)
            .set({
                assignmentId,
                studentId,
                title: assignmentTitle,
                gradePercent: afterGrade,
                sendAt: Timestamp.fromMillis(
                    Date.now()
                ),
                sent: false,
                type: "grade_added",
                createdAt: FieldValue.serverTimestamp(),
            });

        logger.info("Grade notification job created", {
            assignmentId,
            studentId,
            jobId,
            gradePercent: afterGrade,
        });
    }
);

export const processAssignmentNotificationJobs = onSchedule(
    {
        schedule: "every 1 minutes",
        timeZone: "Europe/Warsaw",
    },
    async () => {
        const now = Timestamp.now();

        logger.info("Processing assignment notification jobs", {
            nowMillis: now.toMillis(),
        });

        const jobsSnapshot = await db
            .collection("assignment_notification_jobs")
            .where("sendAt", "<=", now)
            .limit(100)
            .get();

        logger.info("Loaded due notification jobs", {
            count: jobsSnapshot.size,
        });

        for (const jobDocument of jobsSnapshot.docs) {
            const job = jobDocument.data();

            if (job.sent === true) {
                continue;
            }

            try {
                if (job.type === "grade_added") {
                    await sendGradeNotification(
                        job.assignmentId as string,
                        job.studentId as string,
                        job.title as string,
                        job.gradePercent as number
                    );
                } else if (job.type === "announcement_created") {
                    await sendAnnouncementNotification(
                        job.announcementId as string,
                        job.courseId as string,
                        job.title as string,
                        job.message as string
                    );
                } else {
                    await sendAssignmentReminder(
                        job.assignmentId as string,
                        job.courseId as string,
                        job.title as string,
                        job.dueDate as Timestamp
                    );
                }

                await jobDocument.ref.update({
                    sent: true,
                    sentAt: FieldValue.serverTimestamp(),
                });

                logger.info("Assignment notification job sent", {
                    jobId: jobDocument.id,
                    assignmentId: job.assignmentId,
                    type: job.type,
                });
            } catch (error) {
                logger.error(
                    "Failed assignment notification job",
                    {
                        jobId: jobDocument.id,
                        error,
                    }
                );
            }
        }
    }
);