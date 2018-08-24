Option Explicit
Dim objParm
Set objParm = Wscript.Arguments
CreateObject("WScript.Shell").Run objParm(0),0,true