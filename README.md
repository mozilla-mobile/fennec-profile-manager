# fennec-profile-manager
Fennec Profile Manager (Backup / Restore)

# Prerequisite (assuming we're in project's root folder)

[1] ```~ touch secret.key```
[2] ```~ echo "secret key example" > secret.key```
[3] ```~ ./gradlew build```

### IMPORTANT NOTE!
### All backups must be signed with the same secret key, otherwise they will be invalid!