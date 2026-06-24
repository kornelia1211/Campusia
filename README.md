**Campusia**
Android application for university schedule management and real-time student-lecturer coordination

**Overview**
The app supports three roles: Student, Lecturer, and Admin.
All actions in the application are role-based, ensuring secure data separation and personalized components

**Student**
Students can:
    - view all courses and the specific courses they are enrolled in
    - enroll in courses
    - search for courses by title, lecturer, and department
    - view the details of each course
    - upload assignment results as PDF files
    - join private and course-specific chats
    - receive notifications about upcoming deadlines and new messages
    - view their personal schedule
    - export their personal schedule directly to an external calendar application via a generated .ics file
    - change personal data (e.g. address, password)

**Lecturer**
Lecturers can:
    - see all courses they are assigned to
    - search for courses by title, lecturer, and department
    - create and edit the courses they conduct
    - add assignments
    - post announcements
    - remove students from courses
    - view students' submitted work and grade it
    - view their teaching schedule
    - join private and course-specific chats
    - export their personal schedule directly to an external calendar application via a generated .ics file
    - change personal data (e.g. address, password)
    - track the number of remaining tasks left to grade
    - see how many students have submitted solutions for a specific task
    - receive notifications about new messages

**Admin**
Admins can do everything a Student and Lecturer can, plus:
    - configure the term start date
    - manage university departments

**Project Structure**
The project follows a following architecture:
    - Screens – Compose components handling declarative UI rendering and layout states
    - Components – reusable UI building blocks (such as navigation bars, headers, cards)
    - Entities – data classes mapped directly to Firebase Firestore schemas
