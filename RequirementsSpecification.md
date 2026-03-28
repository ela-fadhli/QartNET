# Requirements Specification
## Project QartNET — Qart Hadasht Network for Engineering & Technology
### Academic Collaborative Technology Platform

| | |
|---|---|
| **Institution** | École Nationale d'Ingénieurs de Carthage (ENICarthage) |
| **Department** | Computer Science Department |
| **Module** | Development Platforms |
| **Academic supervisors** | Mr. Faouzi Jaidi / Ms. Imen Kammoun |
| **Academic year** | 2025 – 2026 |

**Team:**
- Azer Kouka
- Idriss Haddad
- Ela Fadhli

---

## Table of Contents

- [1. Introduction](#1-introduction)
- [2. Project Context](#2-project-context)
- [3. System Scope](#3-system-scope)
- [4. Global Architecture](#4-global-architecture)
- [5. Functional Requirements](#5-functional-requirements)
- [6. Non-Functional Requirements](#6-non-functional-requirements)
- [7. Constraints](#7-constraints)
- [8. Future Evolutions](#8-future-evolutions)

---

## 1. Introduction

### 1.1 Purpose of this Document

This requirements specification defines the full set of functional, non-functional, technical, and organizational specifications for Project QartNET. It serves as the contractual reference describing the system's requirements, scope, constraints, and strategic objectives.

### 1.2 General Project Overview

Project QartNET is a collaborative web platform oriented toward digital technologies, designed for the academic community of the institution. Its purpose is to structure an internal ecosystem that promotes technical exchanges, collaboration on computer science projects, and academic hosting of Git repositories.

The platform will combine the features of an academic social network with an integrated project management and versioning system. It will enable the publication of technical content, the organization of working groups, project review, and communication between students and teachers.

The system is designed as a production-deployable solution with an evolutionary architecture that allows future extension toward mobile services or multi-institution deployment.

---

## 2. Project Context

### 2.1 Problem Statement

Currently, the institution does not have a centralized digital environment dedicated to structured technical exchanges or academic collaboration on computer science projects. Interactions between students and teachers take place through scattered channels, which limits coherence, traceability, and knowledge capitalization.

The absence of an internal project hosting system also restricts the establishment of a genuine technological community dynamic, reducing opportunities for peer support, code review, and cross-cohort collaboration.

### 2.2 Objectives

The project aims to establish an internal digital infrastructure that structures a genuine academic technology community. It must facilitate collaboration on projects, encourage code review, and offer a Git hosting system comparable to professional standards, adapted to the educational context.

The objective is also to strengthen interactions between students and teachers by creating a digital space that promotes mutual support, knowledge sharing, and the recognition of completed work.

### 2.3 Stakeholders

The main stakeholders of the project are students, teachers, and the institution's administration.

---

## 3. System Scope

### 3.1 Target Users

The platform will be accessible to all students and teachers holding a valid institutional email address. All users will benefit from the same privileges within the collaborative social space.

Administrative functions will be accessible exclusively through a distinct interface reserved for authorized members.

### 3.2 Expected Capacity

The system must support an initial capacity of at least **1,000 active users**. The architecture must be designed in an evolutionary manner to allow progressive scaling without major system overhaul.

---

## 4. Global Architecture

### 4.1 Technology Stack

The user interface will be developed using the **Angular** framework, ensuring modularity, maintainability, and client-side performance. The backend will rely on **Spring Boot** to implement robust and secure REST APIs.

Data persistence will be provided primarily by a **PostgreSQL** relational database. The complementary use of a NoSQL database such as **MongoDB** may be considered for managing unstructured data if needed.

**Git system integration** will be a central component of the architecture, enabling repository management and versioning operations.

The project will be conducted according to an **agile Scrum** methodology, promoting iterative and incremental development.

### 4.2 Logical Architecture

The system will adopt a layered architecture comprising:

- Presentation layer
- REST API layer
- Business logic layer
- Data access layer
- A dedicated layer for Git repository management and associated service integration

External services such as the mail server will be integrated via secure, decoupled interfaces.

---

## 5. Functional Requirements

### 5.1 Authentication and User Management
*Owner: Idriss Haddad*

The system must allow user registration exclusively via an institutional email address. Any account creation must be validated through an email confirmation mechanism before final activation.

The platform must ensure secure session management, including login, logout, and password reset. Each user will have an editable profile presenting their academic information, projects, and contributions.

### 5.2 Project Hosting and Git Integration
*Owner: Azer Kouka*

The platform must allow the creation and management of internal Git repositories. Authorized users must be able to perform standard operations such as push, pull, and commit history consultation.

The system must manage repository visibility, allowing them to be defined as public or private. It must display associated metadata, identify contributors, and guarantee the integrity of versioned data.

### 5.3 Project Review System
*Owner: Azer Kouka*

The platform must allow the publication of comments and the conducting of structured reviews on hosted projects. Teachers must be able to provide formalized academic feedback. Discussions associated with a project must be organized in a clear and traceable manner.

### 5.4 Project Phase Management
*Owner: Azer Kouka*

The system must allow the structuring of projects into phases, the definition of tasks, and the establishment of milestones. It must offer a progress tracking mechanism enabling collaborative visualization of the project's state.

### 5.5 Forum Module
*Owner: Ela Fadhli*

The platform must integrate a discussion module allowing the publication of content, replies to posts, and the organization of exchanges by thematic categories. A tag system must allow topics to be classified according to specific technological domains.

### 5.6 Groups
*Owner: Ela Fadhli*

Users must be able to create public or private groups to organize discussions and collaborative work. The system must allow member invitations and access management.

### 5.7 Messaging
*Owner: Ela Fadhli*

The platform must integrate private messaging allowing individual exchanges as well as group conversations. Communication must be ensured in real time or near real time.

### 5.8 Notifications
*Owner: Ela Fadhli*

The system must notify the user upon significant events such as receiving a comment, a message, a group invitation, or a project review.

### 5.9 Search
*Owner: Ela Fadhli*

A search feature must allow the identification of users, projects, and publications. Filtering mechanisms by categories and tags must be available.

### 5.10 Administrative Backoffice
*Owner: Idriss Haddad*

A distinct administrative interface must allow user management, account suspension, content moderation, and global monitoring of platform activity.

---

## 6. Non-Functional Requirements

### 6.1 Performance

The system must guarantee a response time of less than **two seconds** for standard operations under normal usage conditions. Operations related to Git repositories must be optimized to ensure a smooth experience.

### 6.2 Security

- The platform must use the **HTTPS** protocol for all communications
- **Role-based access control** must be implemented to restrict sensitive features
- Private repositories must be protected against unauthorized access
- Passwords must be stored as **secure hashes**
- The system must be protected against the main application vulnerabilities such as **SQL injection** and **XSS attacks**

### 6.3 Availability

The system must be accessible continuously, except during planned maintenance windows. Backup and restoration mechanisms must be put in place to ensure service continuity.

### 6.4 Scalability

The architecture must allow **horizontal scaling** and be compatible with deployment in a cloud environment.

### 6.5 Data Confidentiality

The system must guarantee the protection of personal data and comply with the institution's internal policies. Private communications must be secured and protected against unauthorized interception.

---

## 7. Constraints

The project must be completed within a **four-month deadline** by a team of **three developers**. The budget being limited and the hosting strategy not yet defined, optimized technical choices must be made to ensure the project's feasibility within the academic framework.

---

## 8. Future Evolutions

The system must be designed in an evolutionary manner to allow:

- Development of a **mobile application**
- Integration of **AI-assisted code review** mechanisms
- Implementation of a **continuous integration pipeline**
- Extension to **multiple institutions**

---

## Conclusion

Project QartNET aims to create a complete, secure, and scalable collaborative technology platform adapted to the academic context. It combines the features of an internal social network, a Git hosting system, and a structured project management environment.

Designed according to rigorous software engineering principles, the system aims to offer a sustainable, deployable solution capable of evolving according to the institution's future needs.