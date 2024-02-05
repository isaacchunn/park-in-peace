# 2006-SCS2-SoftwareSquad

# Project Directory

```
.                           # Root directory
├── androidApp              # Source files for the `client`
├── docs                    # Documentation files for each lab milestone
├── server                  # Source files for `server`.
├── shared                  # Source files for `common` and `server`
├── build.gradle.kts        # Build tool & configuration (gradle)
├── SECRETS.properties      # Secrets (.gitignore)
├── android.ENV.properties  # Server URL (.gitignore)
└── README.md
```

## Notes

> [!tip] **For lab deliverables**...
>
> Please head directly to [docs/Lab 5](docs/Lab%205).


---

![](docs/Lab%205/Architecture%20Diagram/client.png)
![](docs/Lab%205/Architecture%20Diagram/server.png)
![](docs/Lab%205/Architecture%20Diagram/apis.png)

---

# Android Studio

## Setup

The repository contains two files [SECRETS.properties.sample](SECRETS.properties.sample)
and [android.ENV.properties.sample](android.ENV.properties.sample).

Please read through the files and fill in the respective tokens and paste them into `SECRETS.properties` and
`android.ENV.properties`.

This step is **MANDATORY** for the app to build.

## Building and Running

1. Import the project root directory into Android Studio/IntelliJ. Do not import the subdirectories or else it will not
   work.
2. Running the android app - select `androidApp` in the run configuration and run
3. Running the server - select `ParkInPeace:server [run]` in the run configuration and run
    - if it is successful
    - try opening http://127.0.0.1:8080 in your browser it should say `{"result": true}`

# Server

Refer to [server/README.md](server/README.md) for instructions.