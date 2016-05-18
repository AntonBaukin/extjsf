#!/usr/bin/env node

const assert = require('assert')
const fs = require('fs')
const zlib = require('zlib')
const lo = require('lodash')

if([3, 4].indexOf(process.argv.length) == -1)
{
	console.error('Usage: ./gen.js file size [seed]')
	process.exit(4)
}

const size = parseInt(process.argv[2])
assert(lo.isNumber(size))

var seed = (process.argv.length == 3)?(new Date().getTime()):parseInt(process.argv[3])
assert(lo.isNumber(seed))

console.error('Generating data of size: %d seed: %d', size, seed)

function genId(o)
{
	if(!genId.$ii) genId.$ii = 0x8d8cb555
	if(!genId.$xx) genId.$xx = '0123456789abcdef'.split('')

	var ii = (genId.$ii++).toString(16)
	var xx = ''; while(xx.length < 12) xx += rand(genId.$xx)

	return lo.extend(o || {}, {
		'_id': ii + '-1c27-11e6-8b6e-' + xx
	})
}

const firstNamesM = readLines('namesm.txt.gz')
const firstNamesW = readLines('namesw.txt.gz')
const lastNames = readLines('sirnames.txt.gz')

function genFullName(o)
{
	var s = (rand(2) == 0)
	var n = lo.capitalize(rand(s?firstNamesM:firstNamesW))
	var l = lo.capitalize(rand(lastNames)).split(/\W/)[0]

	return lo.extend(o || {}, {
	  firstName: n, lastName: l, sex: (s?'M':'F')
	})
}

function genEmail(o)
{
	assert(o && o.firstName && o.lastName)

	o.email = lo.toLower(o.firstName) +
	  rand([ '.', '-' ]) + lo.toLower(o.lastName) + '@' +
	  rand([ 'gmail.com', 'yahoo.com', 'aol.com', 'box.com' ])

	return o
}

const states = lo.keyBy(readJSON('states.json.gz'), 'code')

function genStatePhone(state)
{
	if(!genStatePhone.$n) genStatePhone.$n = '23456789'.split('')
	if(!genStatePhone.$x) genStatePhone.$x = '0123456789'.split('')

	var s = states[state]
	assert(s)

	var x = [ rand(genStatePhone.$n), rand(genStatePhone.$x), rand(genStatePhone.$x) ]
	if(x[1] == '1' && x[2] == '1') x[2] = rand(genStatePhone.$n)

	return '+1 ' + rand(s.phoneCodes) + ' ' + x.join('') +
	  ' ' + rands(genStatePhone.$x, 4).join('')
}

var cities = readJSON('cities.json.gz')
var streets = readLines('streets.txt.gz')

function genAddress()
{
	if(!genAddress.$09) genAddress.$09 = '0123456789'.split('')

	if(!genAddress.$sa) genAddress.$sa = [
	  'alley', 'ave', 'circle', 'drive', 'loop', 'pass',
	  'road', 'route', 'square', 'street', 'walk', 'way'
	]

	var c = rand(cities)
	var s = states[c.state]
	var w = rand(streets) + ' ' + lo.capitalize(rand(genAddress.$sa))
	var b = rands(genAddress.$09, 1 + rand(3))
	if(b[0] == '0') b[0] = '' + (1 + rand(9))

	var a = { state: s.code, city: c.city, zip: c.zip,
	  street: w, building: b.join('')
	}

	if(rand(4) == 0) a.phone = genStatePhone(s.code)

	return a
}

function genPerson()
{
	var p = genEmail(genFullName(genId()))
	var a = genAddress()

	p.phone = genStatePhone(a.state)
	p.addess = a

	return p
}


for(let i = 0;(i < 5);i++)
	console.log(genPerson())


function rand(/* n | array | map */)
{
	seed = (2147483629 * seed + 2147483587) % 2147483647

	var a = arguments[0]

	if(lo.isNumber(a))
		return seed % a

	if(lo.isArray(a))
		return a[seed % a.length]

	if(lo.isObject(a))
	{
		var ks = lo.keys(a)
		return a[ks[seed % ks.length]]
	}
}

function rands(/* rand() argument, times */)
{
	var n = arguments[1]
	assert(lo.isNumber(n))

	for(var r = [], i = 0;(i < n);i++)
		r.push(rand(arguments[0]))
	return r
}

function scope(/* [parameters] f */)
{
	var f = arguments[arguments.length - 1]
	assert(lo.isFuntion(f))

	//?: {has additional arguments}
	for(var a = [], i = 0;(i < arguments.length - 1);i++)
		a.push(arguments[i])

	return (a.length)?(f.apply(this, a)):(f.call(this))
}

function readFile(file)
{
	if(!lo.endsWith(file, '.gz'))
		return fs.readFileSync(file, { encoding: 'UTF-8' })
	else
		return zlib.gunzipSync(fs.readFileSync(file)).toString('UTF-8')
}

function readLines(file)
{
	var s = readFile(file)
	assert(lo.isString(s))

	s = s.split('\n')
	for(let i = 0;(i < s.length);i++)
		if(!(s[i] = s[i].trim()).length)
			s.splice(i, 1)

	return s
}

function readJSON(file)
{
	var s = readFile(file)
	assert(lo.isString(s))

	var o = JSON.parse(s)
	assert(lo.isObject(o))

	return o
}

process.exit(0)