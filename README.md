# Fennec Profile Manager
--
#### Internal application that allows *Backup / Restore* of Fennec's internal data
###### (including but not limited to: open tabs / history / bookmarks / passwords / app settings)
#### To be used for testing various Fenix migration scenarios.

## How to build
For being able to create and restore Fennec Backups, FPM must be signed with the same signing key as Fennec.
### Guide to build variants
There are currently 4 build variants, all mirroring Fennec's distribution channels.  
Each such flavour is configured to know for what Fennec application to create / restore a backup.

- **dev** - Developer builds. Useful if you want to test migration using locally produced fennec builds.  
FPM app's *build.gradle* will have to be edited to replace the default _*org.mozilla.fennec\_andrei.a.lazar*_ Fennec package name with the one of your local Fennec build.
- **nightly** - FPM builds that knows how to create and restore official Fennec _Nightly_ backups.
- **beta** - FPM builds that knows how to create and restore official Fennec _Beta_ backups.
- **prod** - FPM builds that knows how to create and restore official Fennec _Nightly_ backups.


## License


    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/
    