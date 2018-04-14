from subprocess import call
import time
time.sleep(11*60*60)
call("sudo shutdown -h now",shell=True)
