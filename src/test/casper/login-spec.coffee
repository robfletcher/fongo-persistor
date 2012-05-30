casper.start 'https://localhost:8080/index.html', ->
    @fill '.sidebar form', username: 'tim', password: 'password', false
    this.captureSelector('sidebar1.png', '.sidebar h3')
    @click '.sidebar form button'
    @waitFor ->
        @visible('.sidebar h3')
    , ->
        @test.assertEquals @fetchText('.sidebar h3'), 'Logged in as tim', 'login message is displayed'
        this.captureSelector('sidebar2.png', '.sidebar h3')
    , ->
        @test.fail 'login message is not visible'
    , 1000

casper.run ->
    @test.done()
