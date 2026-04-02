package project.gigienist_reports.feature.users.domain.errors

class AdminAuthorityNotFoundException : RuntimeException("Admin authority not found. Not enough permissions to execute.")