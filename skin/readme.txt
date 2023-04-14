CILogon colors are

light green arrow fill: 182,215,168 -> [b6,d7,a8]
dark green arrow fill: 145,171,134 -> [91,ab,86]
arrow outline: 39,78,19 -> [27,4e,13]
CILogon text: 102,153,102 -> [66,99,66]

Done with QDL:
$$HEX. := (@to_string∀[[;10]])~['a','b','c','d','e','f']; // create hex digits
to_hex(x)->$$HEX.(x%16) + $$HEX.(x - 16*(x%16));

   @to_hex∀[[182,215,168]]
[b6,d7,a8]