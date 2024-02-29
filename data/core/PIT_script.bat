@echo on
echo command:
echo java -cp D:\Coding\Creative\group_project_I\data\lib\PIT\*;D:\Coding\Creative\group_project_I\data\lib\Junit5\*;D:\Coding\Creative\group_project_I\data\lib\Evosuite\*;D:\Coding\Creative\group_project_I\data\targets;D:\Coding\Creative\group_project_I\data\tests;
echo         org.pitest.mutationtest.commandline.MutationCoverageReport \
echo         --reportDir D:\Coding\Creative\group_project_I\data\PITReport
echo         --targetClasses Customer,Product,SortBy,Store
echo         --targetTests com.your.package.*
echo         --sourceDirs D:\Coding\Creative\group_project_I\data
echo         --mutableCodePaths D:\Coding\Creative\group_project_I\data\targets
echo         --targetTests TestOnlineStoreLocal
echo         --outputEncoding UTF-8
echo running...

cd -q D:\Coding\Creative\group_project_I\data\lib\PIT\*;D:\Coding\Creative\group_project_I\data\lib\Junit5\*;D:\Coding\Creative\group_project_I\data\lib\Evosuite\*

java -version

java -cp D:\Coding\Creative\group_project_I\data\lib\PIT\*;D:\Coding\Creative\group_project_I\data\lib\Junit5\*;D:\Coding\Creative\group_project_I\data\lib\Evosuite\*;D:\Coding\Creative\group_project_I\data\targets;D:\Coding\Creative\group_project_I\data\tests; org.pitest.mutationtest.commandline.MutationCoverageReport --reportDir D:\Coding\Creative\group_project_I\data\PITReport --sourceDirs D:\Coding\Creative\group_project_I\data --targetClasses Customer,Product,SortBy,Store --mutableCodePaths D:\Coding\Creative\group_project_I\data\targets --targetTests TestOnlineStoreLocal --outputEncoding UTF-8

echo done
