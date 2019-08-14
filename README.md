# MyPasswordManager

![GitHub Logo](/images/logo.png)

**MyPasswordMananger** is a free, open-source, portable and easy to use password manager.
MyPM is written in Java and uses JavaFX as front-end.

## Images:
![GitHub Logo](/images/splitView.png)

![GitHub Logo](/images/tableView.png)

You can find other images in *MyPasswordManager/images*

## Download:
* [Java .jar](https://github.com/davidecocca/MyPasswordManager/releases/download/v1.0/MyPM_v1.0.jar)
* [Windows .exe](https://github.com/davidecocca/MyPasswordManager/releases/download/v1.0/MyPM_v1.0.exe)
* MacOS .app *(Work in progress)*

## How to execute:

### Windows
* Install JRE 8
* Download [MyPM_v1.0.exe](https://github.com/davidecocca/MyPasswordManager/releases/download/v1.0/MyPM_v1.0.exe) file or [MyPM_v1.0.jar](https://github.com/davidecocca/MyPasswordManager/releases/download/v1.0/MyPM_v1.0.jar) file
* Double-click on the icon

### Linux
* Install OpenJDK and OpenJFX
* Download [MyPM_v1.0.jar](https://github.com/davidecocca/MyPasswordManager/releases/download/v1.0/MyPM_v1.0.jar) file
* Go to the download folder and open a new terminal window
* <code>sudo chmod +x MyPM.jar</code>
* <code>java -jar MyPM.jar </code>

### MacOS
* Install JRE 8
* Download [MyPM_v1.0.jar](https://github.com/davidecocca/MyPasswordManager/releases/download/v1.0/MyPM_v1.0.jar) file
* Double-click on the icon

## Features:
* Manage credentials
* Save credentials into an AES-256 encrypted file
* Generate secure random passwords from 8 to 32 characters
* Import credentials from .csv, .xls and .xlsx
* Export credentials to .cvs, .xls and .xlsx
* 2 types of view: SplitView or TableView

## Dependences:
* JDK 1.8 or later
* JavaFX library
* JFoenix library for "Google Material Design" style Java components (Apache 2.0)
https://github.com/jfoenixadmin/JFoenix
* Passay library for secure random password generation (Apache 2.0 and LGPL)
https://github.com/vt-middleware/passay
* Apache POI for Excel integration (Apache 2.0)
https://github.com/apache/poi

## Cryptography:
MyPM uses an AES-256 encryption for saving credentials on file + PBKDF2 as key derivation function (so you don't need to use password 16-characters long).

## Why:
* I needed a portable and multi-platform (Windows, MacOS and Linux) password manager, with encryption feature and local saving
* For fun and to learn

## License:
Apache 2.0

## Notes:
* This is a personal project so there are no guarantees on security and stability side
* Feel free to fork it and tailor it to your needs
* All contributions are appreciated :)

## Credits:
App icon made by Gregor Cresnar from www.flaticon.com
