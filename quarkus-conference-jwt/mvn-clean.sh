#!/bin/bash

pushd ~/workspace/GITHUB/DO378-apps/labs/content.example.com/courses/do378/rhboq1.7/materials/solutions/secure-review/quarkus-conference/

echo "Cleaning the 'microservice-jwt' project "
cd microservice-jwt
mvn clean
sleep 5
cd ..

echo "Cleaning the 'microservice-speaker' project "
cd microservice-speaker
mvn clean
sleep 5
cd ..

echo "Cleaning the 'microservice-session' project "
cd microservice-session
mvn clean
sleep 5
cd ..

echo "All services cleaned"
echo

popd
