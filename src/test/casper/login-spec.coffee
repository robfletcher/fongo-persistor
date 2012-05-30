casper.start 'http://localhost:8080/index.html', ->
    @fill '.sidebar form', username: 'tim', password: 'password', false
    this.captureSelector('sidebar1.png', '.sidebar h3')
    @click '.sidebar form button'
    @wait 1000, ->
        @test.assertEquals @fetchText('.sidebar h3'), 'Logged in as tim', 'Login text is displayed'
        this.captureSelector('sidebar2.png', '.sidebar h3')

casper.run ->
    @test.done()
