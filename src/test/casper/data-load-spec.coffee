getAlbums = ->
    albums = document.querySelectorAll "#shop table tbody td:nth-child(3)"
    Array::map.call albums, (e) -> e.innerText

albums = []

casper.start 'http://localhost:8080/index.html', ->
    @waitForSelector '#shop table tbody tr:nth-child(4)', ->
        albums = @evaluate getAlbums
        @test.assertEquals albums[0], 'I Am A Cider Drinker', 'album title is correct'
        @test.assertEquals albums[1], 'Ice Ice Baby', 'album title is correct'
        @test.assertEquals albums[2], 'The Happy Hammond', 'album title is correct'
        @test.assertEquals albums[3], 'The Birdy Song', 'album title is correct'

casper.run ->
    @test.done()
