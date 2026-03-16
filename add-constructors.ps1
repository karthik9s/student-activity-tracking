# Add constructors to services that need them

# StudentService
$file = "backend/src/main/java/com/college/activitytracker/service/StudentService.java"
$content = Get-Content $file -Raw
if ($content -notmatch 'public StudentService\(') {
    $content = $content -replace '(private final AuditLogService auditLogService;)\s*\n\s*\n\s*(@Transactional)', '$1

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, StudentMapper studentMapper, AuditLogService auditLogService) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.studentMapper = studentMapper;
        this.auditLogService = auditLogService;
    }

    $2'
    Set-Content $file $content
    Write-Host "Added constructor to StudentService"
}

# AuthService
$file = "backend/src/main/java/com/college/activitytracker/service/AuthService.java"
$content = Get-Content $file -Raw
if ($content -notmatch 'public AuthService\(') {
    $content = $content -replace '(private final BruteForceProtectionService bruteForceProtectionService;)\s*\n\s*\n\s*(@Value)', '$1

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider, BruteForceProtectionService bruteForceProtectionService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }

    $2'
    Set-Content $file $content
    Write-Host "Added constructor to AuthService"
}

# AuditLogService
$file = "backend/src/main/java/com/college/activitytracker/service/AuditLogService.java"
$content = Get-Content $file -Raw
if ($content -notmatch 'public AuditLogService\(') {
    $content = $content -replace '(private final AuditLogRepository auditLogRepository;)\s*\n\s*\n\s*(@Transactional)', '$1

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    $2'
    Set-Content $file $content
    Write-Host "Added constructor to AuditLogService"
}

# CustomUserDetailsService
$file = "backend/src/main/java/com/college/activitytracker/security/CustomUserDetailsService.java"
$content = Get-Content $file -Raw
if ($content -notmatch 'public CustomUserDetailsService\(') {
    $content = $content -replace '(private final UserRepository userRepository;)\s*\n\s*\n\s*(@Override)', '$1

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    $2'
    Set-Content $file $content
    Write-Host "Added c