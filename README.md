# [zorge_file](https://github.com/nar314/zorge_file) - File encrypor/decryptor

## What is this.

This is utility to encrypt and decrypt files using GUI or command line.

## How to run in GUI mode.

```sh
java -jar zorge_file-1.0.jar
```
![dialog](https://github.com/user-attachments/assets/9c29dd3c-dbda-42cf-a40e-26fb31807799)

## How to run in command line mode.

```sh
java -jar zorge_file-1.0.jar /?

Zorge file. Utility to encrypt or decrypt files.
Usage: [encr/decr] <password> <source file> <destination file> [-o] [-i]
'-o' - overwrite target file, no overwrite by default.
'-i' - interactive mode to enter password. You have to provide fake password for interactive mode.

java -jar zf.jar encr myPsw1 file1.txt file1.encr
java -jar zf.jar decr myPsw1 file1.encr file1.txt

```

