package project.gigienist_reports.feature.users.domain.errors

class UserDuplicateLoginException : RuntimeException("Duplicate email or phone number. Please try different input.")