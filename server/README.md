1. See [docs/documentation.pdf](docs/documentation.pdf) for API Specification.
2. See [docs/README.http](docs/README.http) for examples of API calls
3. See [src/.../documentation.yaml](src/commonMain/resources/openapi/documentation.yaml) for raw OpenAPI specification.
   - NOTE: please use a renderer like [Swagger](https://editor-next.swagger.io) or you may run into rendering issues
   - the plugin included with the server is outdated and unable to render the OpenAPI specification properly.

# Deployment instructions

A live version of the server is running on the school's VM at http://172.21.148.166:80. Note that queries are slower
than normal due to connection troubles between Mapbox and the VM.

```bash
ssh -A VMuser@172.21.148.166

# Update code
cd 2006-SCS2-SoftwareSquad
git pull

# Activate firewall rules
sudo ufw allow 80/tcp
sudo ufw allow 8080/tcp
sudo ufw route allow from any port 80 to 127.0.0.1 port 8080
sudo ufw status

# Keep server running in background
tmux new-session

export ANDROID_MAPBOX_SECRET_TOKEN=sk...
export SERVER_MAPBOX_ACCESS_TOKEN=pk...
export SERVER_URA_ACCESS_TOKEN=<SAME-AS-ANDROID>
export SERVER_ONEMAP_USERNAME=<SIGNUP EMAIL>
export SERVER_ONEMAP_PASSWORD=<PASSWORD>
export SERVER_LTA_ACCOUNT_KEY=<ACCOUNT-KEY>

cd server
../gradlew installDist # build release binary
build/install/server/bin/server # start the server
```

1. SSH into server
2. Clone from GitHub
3. Set up firewall rules to allow external connections to connect
4. Use a terminal multiplexer such as tmux to keep server running when you logout
5. Set up environment variables in the current environment (alternatively,
   use [SECRETS.properties](../SECRETS.properties.sample))
6. Build a release-build of the server using gradle
7. Run the server

# Development build

```bash
cd server
../gradlew run # build development-server and run
```

# Logging

- logs are by default logged to `server/app.log` and `server/errors.log`

# Database

SQLite. `server/parkinpeace.db`. Configurable
in [Application.kt](src/commonMain/kotlin/ntu26/ss/parkinpeace/server/Application.kt).