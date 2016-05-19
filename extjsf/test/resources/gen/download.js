const fs = require('fs')
const http = require('http')

var total = 20000
var index = 0
var PAGE = 100
var URL = 'http://www.shopstyle.com/api/v2/site/search?cat=women&device=desktop&includeCategoryHistogram=false&includeProducts=true&locales=en_US&pid=shopstyle&track=false&url=/browse/women&view=angular&limit='+PAGE
//'http://www.shopstyle.com/api/v2/site/search?device=desktop&includeCategoryHistogram=false&includeProducts=true&locales=en_US&pid=shopstyle&url=/browse/men&view=angular&limit=' + PAGE

var DIR = '/Users/anton/Downloads/women/'

function getNext()
{
	if(index * PAGE >= total)
		return console.log('Done with %d files!', index)

	var url = URL + '&offset=' + (PAGE * index++)

	var bufs = []
	http.get(url, function(res)
	{
		res.on('error', function(err)
		{
			console.error(err)
			bufs = null
		})

		res.on('end', function()
		{
			if(bufs == null) return
			var buf = Buffer.concat(bufs)

			saveNext(buf)
		})

		res.on('data', function(chunk)
		{
			if(bufs == null) return
			bufs.push(new Buffer(chunk))
		})
	})
}

function saveNext(buf)
{
	var file = '' + (index-1)
	while(file.length < 3) file = '0' + file
	file = DIR + file + '.json'

	fs.writeFile(file, buf, function(err)
	{
		if(err) return console.error(err)

		console.log('Saved %s', file)
		getNext()
	})
}

getNext()
