# Examples using Lrrr

## Golfing problems

### Smoothness
[[Link](https://codegolf.stackexchange.com/questions/180928/find-how-smooth-a-number-is-based-on-binary/180933#180933)]

We can solve this problem naively using `b{$f"01",$f"10"+LȤ{§L*$` (23 bytes)

Explanation:
```
    (implicit input)
       b                     convert input to binary
        {                    new context
         $f"01",$f"10"+      find instances of "01" and "10" in the binary, add the result list
          LȤ                 get the length of the list, inverse the number
            {                new context
              §L             get the binary string's length
                *            multiply
                 $           by the value obtained with LȤ
            
```
This is, again, a very naive solution and can be improved to save multiple bytes