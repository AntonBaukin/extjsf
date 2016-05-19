const assert = require('assert')
const fs = require('fs')
const lo = require('lodash')

function readFile(file)
{
	if(!lo.endsWith(file, '.gz'))
		return fs.readFileSync(file, { encoding: 'UTF-8' })
	else
		return zlib.gunzipSync(fs.readFileSync(file)).toString('UTF-8')
}

function readJSON(file)
{
	var s = readFile(file)
	assert(lo.isString(s))

	var o = JSON.parse(s)
	assert(lo.isObject(o))

	return o
}

var files = []
findFiles('/Users/anton/Downloads/women/')
findFiles('/Users/anton/Downloads/men/')

function findFiles(dir)
{
	for(let i = 0;;i++)
	{
		var f = '' + i
		while(f.length < 3) f = '0' + f
		f = dir + f + '.json'

		try
		{
			fs.accessSync(f, fs.R_OK)
			files.push(f)
		}
		catch(e)
		{
			break
		}
	}
}

var products = []

function procFile(f)
{
	var xo = readJSON(f)
	var pr = xo.products

	assert(lo.isArray(pr))
	pr.forEach(p =>
	{
		delete p.promotionalDeal
		delete p.urlIdentifier
		delete p.image
		delete p.alternateImages
		//delete p.description

		if(p.unbrandedName) {
			p.name = p.unbrandedName
			delete p.brandedName
			delete p.unbrandedName
		}

		procItem(p)
	})

	products.push.apply(products, pr)
}

function procItem(i, o, p)
{
	if(lo.isObject(i))
		return lo.keys(i).forEach(k => procItem(i[k], i, k))

	if(lo.isString(i))
		if(i.startsWith('http://') || i.startsWith('https://'))
			delete o[p]
}

files.forEach(procFile)
console.log('Goods found: %s', products.length)

fs.writeFileSync('/Users/anton/Downloads/goods.src.json', JSON.stringify(products, null, '\t'))