var BufferedReader = java.io.BufferedReader;
var String = java.lang.String;
var InputStreamReader = java.io.InputStreamReader;

var process = java.lang.Runtime.getRuntime().exec('pwd');

var br = new BufferedReader(new InputStreamReader(process.getInputStream()));
var line;
while ((line = br.readLine()) !== null) {
    print(line);
}