casper.start 'https://localhost:8080/index.html', ->
    @test.assertHttpStatus 200, 'page loads successfully'
    @test.assertExists '.hero-unit h1', 'page header is displayed'
    @test.assertEquals @fetchText('.hero-unit h1'), 'Welcome to vToons', 'page header is correct'
    @test.assertTitle 'Welcome to vToons', 'page title is correct'

casper.run ->
    @test.done()
