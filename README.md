# Account Service
A REST API that is primarily used to manage employee payrolls. This project was built with:
- JDK 17
- Spring Boot 3
- Spring Data JPA
- Spring Security
- Spring Web
- H2 Database

## Actors
There are four types of people who interact with the system:
- <b>User</b> : An employee who registers into the system to access their payroll information
- <b>Accountant</b> : An employee who manages all payroll information
- <b>Administrator</b> : An employee with special privileges for managing employee accounts
- <b>Auditor</b> : An employee who reviews all event activity coming from employee accounts

## Authentication
The service uses HTTPS for secure communication and returns a self-signed TLS certificate for the client to encrypt their login data. 
### Incorrect Credentials
If a password mismatch is detected, a failed login attempt event will be generated and saved to the database. The service uses a cache to record 
the number of times each employee fails to login to their account. If a 5th attempt is recorded, the system interprets a brute force attack. 
A brute force event is then generated and the employee account is promptly locked. A locked employee event is also generated. At this point only 
the administrator can unlock the account for security purposes.

## Authorization
The following is a chart showing which actor has access to which endpoint:
![securityAccess](https://github.com/user-attachments/assets/11b73b10-437b-4e7b-acbc-2ee2a4f2a405)

## Business Journeys
The following list describes what each endpoint is used for:
- <b>POST api/auth/signup</b>: registers an employee in the service
- <b>POST api/auth/changepass</b>: enables an employee to change the account password
- <b>POST api/acct/payments</b>: uploads payroll information from accountant
- <b>GET api/empl/payment</b>: fetches payroll information for a given employee
- <b>GET api/admin/user</b>: fetches all employee information for an administrator
- <b>GET api/security/events</b>: fetches all generated events from the service
- <b>PUT api/acct/payments</b>: modifies employee payroll salary of a specific period
- <b>PUT api/admin/user/role</b>: modifies the employee role in the service
- <b>PUT api/admin/user/access</b>: modifies the employee access in the service
- <b>DELETE api/admin/user</b>: removes an employee from the service

## Events
This system classifies events under two categories - <b>Security</b> and <b>Journey</b>. Event handlers execute asynchronously and are decoupled 
from the business workflows.

### Journey Events
- <b>CreateEmployeeEvent</b>: generated after an employee is registered with the service
- <b>ChangePasswordEvent</b>: generated after an employee changes their login password
- <b>GrantRoleEvent</b>: generated after an administrator grants a new role to an employee
- <b>RemoveRoleEvent</b>: generated after an administrator removes a role from an employee
- <b>DeleteEmployeeEvent</b>: generated after an employee is deleted from the service by an administrator
- <b>LockEmployeeAccessEvent</b>: generated after an employee is locked from the service
- <b>UnlockEmployeeAccessEvent</b>: generated after an employee is unlocked from the service

### Security Events
- <b>AuthenticationFailureBadCredentialsEvent</b>: generated after a failed login attempt through an incorrect password
- <b>AuthenticationSuccessEvent</b>: generated after a successful login attempt
- <b>AuthorizationDeniedEvent</b>: generated after an employee attempts to access unauthorized resources
