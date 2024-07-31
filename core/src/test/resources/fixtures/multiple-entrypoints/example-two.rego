package example.two

import data.example.two.coolRule

default theirRule = false

default ourRule = false

theirRule {
	input.anyProp == "aValue"
}

ourRule {
	input.ourProp == "inTheMiddleOfTheStreet"
}
