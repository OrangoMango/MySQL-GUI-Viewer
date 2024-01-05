# Compile code
echo "Compiling source code..."
javac --module-path $FX_PATH --add-modules javafx.controls -cp .:mysql-connector-java-8.0.28.jar -Xlint:unchecked com/orangomango/mysqlgui/Main.java

# Check if compilation returned an error
STATUS="${?}"

if [ $STATUS -eq "1" ]; then
	exit $STATUS
fi

# Run code
echo "Executing compiled classes..."
java --module-path $FX_PATH --add-modules javafx.controls -cp .:mysql-connector-java-8.0.28.jar com.orangomango.mysqlgui.Main

# Delete .class files
echo "Removing .class files..."
rm com/orangomango/mysqlgui/*.class
echo "Done"
