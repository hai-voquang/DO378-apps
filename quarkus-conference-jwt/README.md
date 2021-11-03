# microservice conference

Quarkus implementation of the Conference management microservice.

## usage

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

### running the application in dev mode

You can run your applications in dev mode that enables live coding using in every subproject module the next command:
```
./start-services.sh
```

Retrieving jwt token:
```
user1_token=$(curl -s -X POST -d @user1.json -H 'Content-Type: application/json' localhost:8084/jwt | jq --raw-output .token)

user2_token=$(curl -s -X POST -d @user2.json -H 'Content-Type: application/json' localhost:8084/jwt | jq --raw-output .token)
```

Calling the session services using token
```
curl -k \
> https://127.0.0.1:8445/sessions/s-1-1 \
> -H 'Content-Type: application/json' -H "Authorization: Bearer ${user1_token}"
{"id":"s-1-1","schedule":1,"speakers":[{"id":1,"name":"Emmanuel Bernard","uuid":"s-1-1"}]}

curl -k \
> https://127.0.0.1:8445/sessions/s-1-1 \
> -H 'Content-Type: application/json' -H "Authorization: Bearer ${user2_token}"
{"id":"s-1-1","schedule":1,"speakers":[{"id":1,"name":"Emmanuel Bernard","uuid":"s-1-1"}]}
```
