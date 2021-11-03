#!/bin/bash

pushd ~/workspace/GITHUB/DO378-apps/labs/content.example.com/courses/do378/rhboq1.7/materials/solutions/secure-review/quarkus-conference/

echo "Starting the 'microservice-jwt' project "
cd microservice-jwt
mvn clean quarkus:dev -Ddebug=5005 &
JWT_PID=$!
sleep 5
cd ..

echo "Starting the 'microservice-speaker' project "
cd microservice-speaker
mvn clean quarkus:dev -Ddebug=5006 &
SPEAKER_PID=$!
sleep 5
cd ..

echo "Starting the 'microservice-session' project "
cd microservice-session
mvn clean quarkus:dev -Ddebug=5007 &
SESSION_PID=$!
sleep 5
cd ..

echo 
read -p "Press enter to Terminate"
echo 
kill $JWT_PID $SPEAKER_PID $SESSION_PID
sleep 2
echo "All services terminated"
echo

popd
