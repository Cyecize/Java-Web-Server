#!/bin/bash

libraries="."
for FILE in bin/*; do 
libraries="$libraries:../$FILE" 
done

echo "$libraries"

cd server
java  -cp "$libraries%" com.cyecize.StartUp