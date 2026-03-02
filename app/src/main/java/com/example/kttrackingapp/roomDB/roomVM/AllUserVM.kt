package com.example.kttrackingapp.roomDB.roomVM

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kttrackingapp.Utils
import com.example.kttrackingapp.roomDB.roomRepo.AllUserRepo
import com.example.kttrackingapp.roomDB.roomTable.AllUserTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class AllUserVM(private val repo : AllUserRepo) : ViewModel() {

    var screenOpt by mutableStateOf(false)

    var loginSignCheck by mutableStateOf(0)

    var username by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var password by mutableStateOf("")


    val passwordRegex =
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!,@,#,$,%,^,&,*,(,),{,},;,:,',<,>,.,?,/]).{8,}$"
    val pass_pattern: Pattern = Pattern.compile(passwordRegex)


    fun ClearData() {
        username = ""
        phoneNumber = ""
        confirmPassword = ""
        password = ""
    }

    fun BtnCheck( check : Int , onSuccess : () -> Unit = {})
    {
        if(check == 0) //login
        {
            println("CHECK---2")
            if(phoneNumber.length == 10)
            {
                if(pass_pattern.matcher(password).matches())
                {
                    CheckPassword(
                        mobileNumber = phoneNumber ,
                        password = password ,
                        onSuccess = {
                            onSuccess()
                        }
                    )
                }
                else
                    Utils.ToastMessage("Invalid Password")
            }
            else
                Utils.ToastMessage("Invalid Phone Number")
        }
        else
        {
            println("CHECK---3")
            if(!username.isNullOrEmpty())
            {
                if(username.length >= 5)
                {
                    if(phoneNumber.length == 10)
                    {
                        if(pass_pattern.matcher(password).matches())
                        {
                            if(password == confirmPassword)
                            {

                                SaveUser(
                                    AllUserTable(
                                        user_id = 0 ,
                                        userName = username ,
                                        userMobile = phoneNumber ,
                                        userPassword = password
                                    ) ,
                                    onSuccess = {
                                        onSuccess()
                                    }
                                )
                            }
                            else
                                Utils.ToastMessage("Password Mismatch")
                        }
                        else
                            Utils.ToastMessage("Password must be at least 8 characters with one special character")
                    }
                    else
                        Utils.ToastMessage("Invalid Phone Number")
                }
                else
                    Utils.ToastMessage("Atleast 5 characters required")
            }
            else
                Utils.ToastMessage("UserName is empty")
        }
    }

    fun SaveUser(item: AllUserTable , onSuccess: () -> Unit) {

        viewModelScope.launch {

            val exists = repo.checkUser(phoneNumber)

            if (exists) {
                Utils.ToastMessage("User Already Exists")
                ClearData()
            } else {
                repo.saveItem(item)
                GetUserId(item.userMobile)
                onSuccess()
                Utils.ToastMessage("User Created Successfully")
            }
        }
    }

    fun checkUser(mobileNumber: String, result: (Boolean) -> Unit) {

        viewModelScope.launch {
            val exists = repo.checkUser(mobileNumber)
            result(exists)
        }
    }


    fun CheckPassword(mobileNumber: String, password: String, onSuccess: () -> Unit)
    {
        viewModelScope.launch {

            val mobileExists = repo.checkUser(mobileNumber)

            if (!mobileExists) {
                Utils.ToastMessage("Mobile number not registered")
                return@launch
            }

            val validLogin = repo.checkLogin(mobileNumber, password)

            if (validLogin) {
                GetUserId(mobileNumber)
                onSuccess()
                Utils.ToastMessage("Login Successful")
            } else {
                Utils.ToastMessage("Incorrect Password")
            }
        }
    }

    fun GetUserId(mobileNumber: String)  {
        viewModelScope.launch {
            val userId = repo.getUserId(mobileNumber)
            Utils.sharedHelper.putBoolean(Utils.activity , Utils.login_Completed , true)
            Utils.sharedHelper.putInt(Utils.activity , Utils.user_id_share , userId)
            Utils.user_id = Utils.sharedHelper.getInt(Utils.activity , Utils.user_id_share)
        }
    }


    private val _userDetail = MutableStateFlow<AllUserTable?>(null)
    val userDetail: StateFlow<AllUserTable?> = _userDetail

    fun getUserData(user_id: Int) {
        viewModelScope.launch {
            _userDetail.value = repo.getUserData(user_id)
        }
    }

}