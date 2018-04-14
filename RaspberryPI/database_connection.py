from firebase import firebase

firebase = firebase.FirebaseApplication('https://wimta-b0aba.firebaseio.com', None)

#Updating checked in according to userId
def updating_checkedIn(userId):
    result = firebase.post('/CheckedIn', userId)

def main():
    
    updating_checkedIn('123456')
    updating_checkedIn('548900')

if __name__ == '__main__':
    main()
    
    
    