# Plutus File Storage Service

This is a simple microservice for Plutus with two endpoints:

- `POST /api/store/book` - Uploads a book file (multipart/form-data ; key: file) to the server and returns a JWT.
- `GET /api/store/book` - Downloads a book file from the server using the corresponding JWT as a Bearer token

File should be uploaded after being encrypted. The service should know nothing of the encryption.

## Build

```bash
./gradlew build
```

The built jar is `build/libs/service-all.jar`

## Run

The application can accept two environment variables:
- `PLUTUS_PORT`: The port to run the service on. Defaults to 8080.
- `PLUTUS_UPLOAD_FOLDER`: The folder to store uploaded files in. Defaults to `./uploads`.
- `PLUTUS_JWT_SECRET`: The secret to use for JWT signing. Defaults to `secret`. (should be overridden)

To run the jar: 

```bash
java -jar build/libs/service-all.jar
```