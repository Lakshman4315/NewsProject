package com.example.materialnewsapp.utils

sealed class Resources<T>
//data: Ye ek nullable generic type T ka property hai, jo data ko store karta hai. Yah
//property data ki present state ko hold karti hai.

//message: Ye ek nullable string property hai, jo kisi message ko store karti hai. Yah
//property kisi specific message ko hold karti hai, jaise error message.
//Resources class ke andar teen inner classes hai: Success, Error, aur Loading. Ye inner
//classes Resources class ke subclasses hai.
    (val data: T? = null, val message: String? = null)
{
    //Success class: Yah Resources class ka subclass hai, jo data parameter ke saath banaya
    //gaya hai. Ismein success state ko represent karne ke liye Resources class ke properties ko
    //inherit kiya jata hai.
    class Sucess<T>(data: T) : Resources<T>(data)

    //Error class: Yah bhi Resources class ka subclass hai, jo message aur data parameters ke
    //saath banaya gaya hai. Ismein error state ko represent karne ke liye Resources class ke
    //properties ko inherit kiya jata hai.
    class Error<T>(message: String, data: T? = null) : Resources<T>(data, message)

    //Loading class: Yah bhi Resources class ka subclass hai. Ismein loading state ko represent
    //karne ke liye koi extra property inherit nahi kiya jata hai
    class Loading<T> : Resources<T>()


    //ah sealed class Resources alag-alag states (success, error, loading) ko represent karti
    //hai. Jaise ki ek API call ka response ho sakta hai:

    //Success state: Jab API call se sahi tarike se data milta hai, toh Success object banaya
    //jata hai. Ismein data property mein sahi tarike se data ko hold kiya jata hai.

    //Error state: Jab API call mein koi samasya hoti hai, toh Error object banaya jata hai.
    //Ismein message property mein samasya se sambandhit message ko hold kiya jata hai aur
    //optional taur par data property mein bhi koi data ho sakta hai.

    //Loading state: Jab API call chal rahi hai aur data aane tak wait kiya ja raha hai, toh
    //Loading object banaya jata hai. Yah state loading ko represent karta hai, jahaan data aur
    //message properties null hoti hai.

    //Is tareeke se Resources sealed class alag-alag states ko represent karti hai aur unhe sahi
    //tarike se handle karne mein madad karti hai
}