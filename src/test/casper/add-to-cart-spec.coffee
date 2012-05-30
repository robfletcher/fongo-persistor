casper.start 'http://localhost:8080/index.html', ->
    @waitForSelector '#shop table tbody tr:nth-child(4)', ->
        @click '#shop tr:nth-child(3) a'
        @test.assertEquals @fetchText('.sidebar h2'), 'Order total: $0.50', 'order total is displayed'

casper.then ->
    @test.assertNot @visible('#cart'), 'cart is not visible'
    @click 'a[href="#cart"]'
    @test.assertTrue @visible('#cart'), 'cart is visible'
    @test.assertEval ->
        $('#cart table tbody tr').length == 1
    , 'cart contains 1 item'

casper.run ->
    @test.done()
