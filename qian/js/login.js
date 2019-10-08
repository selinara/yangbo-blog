//切换
$(document).on("click",".goReg",function(){
    $(this).hide();
    $(this).parent().find(".error_info").hide();
    $(".goLogin").show();
    $(".goForget").show();
    $(".phone_login").show();
    $(".loginAccunt_validate").hide();
    $(".loginForget_validate").hide();
    $(".loginPhone_validate").show();
    $('.login_left h3 span').html('注&nbsp;册');

});
$(document).on("click",".goLogin",function(){
    $(this).hide();
    $(".goReg").show();
    $(this).parent().find(".error_info").hide();
    $(".account_login").show();
    $(".loginPhone_validate").hide();
    $(".loginForget_validate").hide();
    $(".loginAccunt_validate").show();
    $('.login_left h3 span').html('登&nbsp;录');
});
$(document).on("click",".goForget",function(){
    $(this).hide();
    $(".goForget").show();
    $(".goLogin").show();
    $(this).parent().find(".error_info").hide();
    $(".account_login").show();
    $(".loginPhone_validate").hide();
    $(".loginAccunt_validate").hide();
    $(".loginForget_validate").show();
    $('.login_left h3 span').html('修改密码');
});

//验证用户名
$(document).on("focus",".input_item input[type='text']",function(){
      if ($(this).val() == $(this).attr("eg")) {
        $(this).val("");
    }
});
$(document).on("blur",".input_item input[type='text']",function(){
      if ($(this).val() == "") {
        $(this).val($(this).attr("eg"));
    }
});

//验证登录密码
$(document).on('focus','input.loginPwd',function(){
   $(this).parent().find(".password_upTxt").hide();
})
$(document).on("click", ".login_validate span.password_upTxt", function () {
        $(this).hide();
        $(this).parent().find(".loginPwd").focus();
    })

$(document).on("blur", ".login_validate input.loginPwd", function () {
    if ($.trim($(this).val()) == ""){
        $(this).parent().find(".password_upTxt").show();
    }
 })

//验证注册密码
$(document).on('focus','input[name="regpsw"]',function(){
   $(this).parent().find(".password_upTxt").hide();
})
$(document).on("click", ".login_validate span.password_upTxt", function () {
        $(this).hide();
        $(this).parent().find("input[name='regpsw']").focus();
    })

$(document).on("blur", ".login_validate input[name='regpsw']", function () {
    if ($.trim($(this).val()) == ""){
        $(this).parent().find(".password_upTxt").show();
    }
 })


// 短信验证码
$(document).on('focus','input.loginCode',function(){
    if ($(this).val() == $(this).attr("eg")) {
        $(this).val("");
    }
   $(this).parents().find(".Login_Uname").hide();
})
$(document).on('blur','input.loginCode',function(){
    if ($(this).val() == "") {
        $(this).val($(this).attr("eg"));
    }
    onblursRegCode(".loginCode");

})

//点击手机验证码
function onblursRegCode(obj){
    obj = $(obj).closest(".login_containter");
   $(obj).find(".error_info").hide();
    var $loginCode = $(".loginCode");
     var loginCode =$loginCode.val().replace(/\s+/g, "").replace($loginCode.attr("eg"), "");
    if (loginCode == "") {
        $(obj).find(".errorloginCode.error_info").html("<i></i>请输入短信验证码").show().addClass('TxtShadow').css("right","-215px");
            return false;
    }
    else {
        return true;
    }
}
/*******************用户登录*****************************/
var LOCK_LoginFormSubmit = false;//防止用户二次提交
function LoginFormSubmit(obj, callBackFun) {
    if (LOCK_LoginFormSubmit) {
        return false;
    }
    obj = $(obj).closest(".login_containter");
    var OK = true;
    if (!CheckLoginName(obj))
        OK = false;
    if (!OK)
        return false;
    LOCK_LoginFormSubmit = true;
    $(obj).find(".loginBtn").disabled = true;
    $(obj).find(".loginBtn").val("登录中...");
   var loginName = $(obj).find(".loginName").val();
   var loginPwd  = encodeURIComponent($(obj).find(".loginPwd").val());
    var $stObj = $(obj).find(".error_info");
    $stObj.hide();
     jsonAjaxJsonp("GET", u+"/login", "loginname=" + loginName + "&password=" + loginPwd , function (data) {
            LOCK_LoginFormSubmit = false;
            $(obj).find(".loginBtn").disabled = false;
            $(obj).find(".loginBtn").val("登录");

            //登录成功之后
             if (data.errorCode == '0') {
                cleanCookie();
                 A.saveUserCookie(data, true);
             }
             else{
                 $(obj).find(".errorloginName.error_info").html("<i></i>"+data.message).show().addClass('TxtShadow').css("right","-240px");
                 $(obj).find(".errorloginName.error_info").parent().addClass('input_item_error');
             }
     }, function (data) {
        LOCK_LoginFormSubmit = false;
         $(obj).find(".loginBtn")[0].disabled = false;
         $(obj).find(".loginBtn").val("登录");
    });
    return false;
}
function CheckLoginName(obj) {

    $(obj).find(".error_info").hide();
    $(obj).find(".input_item").removeClass('input_item_error');
    if (typeof obj== "undefined") {
        var $loginname = $(".loginName");
        var loginname =$loginname.val().replace(/\s+/g, "").replace($loginname.attr("eg"), "");
        var $pwd = $(".loginPwd");
        var pwd = $pwd.val().replace(/\s+/g, "").replace($pwd.attr("eg"), "");
        if (loginname == ""&&pwd=="") {
           $(obj).find(".errorloginName.error_info").html("<i></i>请输入账号和密码").show().addClass('TxtShadow').css("right","-215px");
           $(obj).find(".errorloginName.error_info").parent().find("input.loginName").val("").focus();
           $(obj).find(".errorloginName.error_info").parent().addClass('input_item_error');

            return false;
        }
        else if (loginname == "") {
           $(obj).find(".errorloginName.error_info").html("<i></i>请输入账号").show().addClass('TxtShadow').css("right","-180px");
            $(obj).find(".errorloginName.error_info").parent().addClass('input_item_error');
            $(obj).find(".errorloginName.error_info").parent().find("input.loginName").val("").focus();
            return false;
        }
        else if (pwd == "") {
            $(obj).find(".errorloginPwd.error_info").html("<i></i>请输入密码").show().addClass('TxtShadow').css("right","-180px");
             $(obj).find(".errorloginPwd.error_info").parent().addClass('input_item_error');
            return false;
        }
        else{
          $(obj).find(".input_item").removeClass('input_item_error');
          return true;
        }


    }
    else{
        var $loginname = $(".loginName");
        var loginname =$loginname.val().replace(/\s+/g, "").replace($loginname.attr("eg"), "");
        var $pwd = $(".loginPwd");
        var pwd = $pwd.val().replace(/\s+/g, "").replace($pwd.attr("eg"), "");
        if (loginname == ""&&pwd=="") {
           $(obj).find(".errorloginName.error_info").html("<i></i>请输入账号和密码").show().addClass('TxtShadow').css("right","-215px");
           $(obj).find(".errorloginName.error_info").parent().find("input.loginName").val("").focus();
           $(obj).find(".errorloginName.error_info").parent().addClass('input_item_error');
            return false;
        }
        else if (loginname == "") {
           $(obj).find(".errorloginName.error_info").html("<i></i>请输入账号").show().addClass('TxtShadow').css("right","-180px");
           $(obj).find(".errorloginName.error_info").parent().find("input.loginName").val("").focus();
           $(obj).find(".errorloginName.error_info").parent().addClass('input_item_error');
            return false;
        }
        else if (pwd == "") {
            $(obj).find(".errorloginPwd.error_info").html("<i></i>请输入密码").show().addClass('TxtShadow').css("right","-180px");
            $(obj).find(".errorloginPwd.error_info").parent().addClass('input_item_error');
            return false;
        }
        else{
        return true;
          $(obj).find(".input_item").removeClass('input_item_error');

        }
    }
}















var username_check = false;
var email_check = false;
var email_checkpp = false;
var psw_check = false;
var psw_checkpp = false;
var code_check = false;
var code_checkpp = false;


//校验用户名唯一性
function onblursCheckName(obj){
    obj = $(obj).closest(".login_containter");
    $(obj).find(".erroruserName").hide();
    var $userName = $("input[name='username']");
    var username =$userName.val().replace(/\s+/g, "").replace($userName.attr("eg"), "");
    if (username == "") {
        $(obj).find(".erroruserName.error_info").html("<i></i>请输入用户名").show().addClass('TxtShadow').css("right","-190px");
        $(obj).find(".erroruserName.error_info").parent().addClass('input_item_error');
        return false;
    }
    else if (!(/^[a-zA-Z0-9_]{4,20}$/.test(username))) {
        $(obj).find(".erroruserName.error_info").html("<i></i>4~20位数字，字母，下划线").show().addClass('TxtShadow').css("right","-270px");
        $(obj).find(".erroruserName.error_info").parent().addClass('input_item_error');
        return false;
    }
    jsonAjaxJsonp("GET", u+"/mail/check/username", "username="+username, function (data) {
        if (data.errorCode == '1') {
            $(obj).find(".erroruserName.error_info").html("<i></i>此用户名已存在").show().addClass('TxtShadow').css("right","-202px");
            $(obj).find(".erroruserName.error_info").parent().addClass('input_item_error');
        } else {
            username_check = true;
        }
    });
}

//邮箱框
function onblursLoginPhone(obj) {
    obj = $(obj).closest(".login_containter");
    $(obj).find(".errorloginPhone").hide();
    var $loginPhone = $("input[name='userEmail']");
    var loginPhone =$loginPhone.val().replace(/\s+/g, "").replace($loginPhone.attr("eg"), "");
    if (loginPhone == "") {
        $(obj).find(".errorloginPhone.error_info").html("<i></i>请输入邮箱").show().addClass('TxtShadow').css("right","-179px");
        $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');
        return false;
    }
    else if (!(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(loginPhone))) {
        $(obj).find(".errorloginPhone.error_info").html("<i></i>请输入正确的邮箱").show().addClass('TxtShadow').css("right","-212px");
        $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');
        return false;
    }
    jsonAjaxJsonp("GET", u+"/mail/check/usermail", "usermail="+loginPhone, function (data) {
        if (data.errorCode == '1') {
            $(obj).find(".errorloginPhone.error_info").html("<i></i>此邮箱已注册").show().addClass('TxtShadow').css("right","-202px");
            $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');
        } else {
            email_check = true;
        }
    });
    return email_check;
}

function onblursLoginPhonePP(obj) {
    obj = $(obj).closest(".login_containter");
    $(obj).find(".errorloginPhonePP").hide();
    var $loginPhone = $("input[name='userEmailPsw']");
    var loginPhone =$loginPhone.val().replace(/\s+/g, "").replace($loginPhone.attr("eg"), "");
    if (loginPhone == "") {
        $(obj).find(".errorloginPhonePP.error_info").html("<i></i>请输入邮箱").show().addClass('TxtShadow').css("right","-179px");
        $(obj).find(".errorloginPhonePP.error_info").parent().addClass('input_item_error');
        return false;
    }
    else if (!(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(loginPhone))) {
        $(obj).find(".errorloginPhonePP.error_info").html("<i></i>请输入正确的邮箱").show().addClass('TxtShadow').css("right","-212px");
        $(obj).find(".errorloginPhonePP.error_info").parent().addClass('input_item_error');
        return false;
    }
    jsonAjaxJsonp("GET", u+"/mail/check/usermail", "usermail="+loginPhone, function (data) {
        if (data.errorCode == '0') {
            $(obj).find(".errorloginPhonePP.error_info").html("<i></i>此邮箱未注册").show().addClass('TxtShadow').css("right","-202px");
            $(obj).find(".errorloginPhonePP.error_info").parent().addClass('input_item_error');
        } else {
            email_checkpp = true;
        }
    });
    return email_checkpp;
}

//验证码
function onblursCheckCode(obj) {
    obj = $(obj).closest(".login_containter");
    $(obj).find(".errorMailPwd").hide();
    var $code = $("input[name='codeval']");
    var code =$code.val();
    if (code == "") {
        $(obj).find(".errorMailPwd.error_info").html("<i></i>请输入验证码").show().addClass('TxtShadow').css("right","-188px");
        $(obj).find(".errorMailPwd.error_info").parent().addClass('input_item_error');
        return false;
    }else if (!(/^[0-9]+$/.test(code))) {
        $(obj).find(".errorMailPwd.error_info").html("<i></i>请输入数字验证码").show().addClass('TxtShadow').css("right","-212px");
        $(obj).find(".errorMailPwd.error_info").parent().addClass('input_item_error');
        return false;
    }
    code_check = true;
    return true;
}
//修改密码-验证码
function onblursCheckCodePP(obj) {
    obj = $(obj).closest(".login_containter");
    $(obj).find(".errorMailPwdPP").hide();
    var $code = $("input[name='codevalPsw']");
    var code =$code.val();
    if (code == "") {
        $(obj).find(".errorMailPwdPP.error_info").html("<i></i>请输入验证码").show().addClass('TxtShadow').css("right","-188px");
        $(obj).find(".errorMailPwdPP.error_info").parent().addClass('input_item_error');
        return false;
    }else if (!(/^[0-9]+$/.test(code))) {
        $(obj).find(".errorMailPwdPP.error_info").html("<i></i>请输入数字验证码").show().addClass('TxtShadow').css("right","-212px");
        $(obj).find(".errorMailPwdPP.error_info").parent().addClass('input_item_error');
        return false;
    }
    code_checkpp = true;
    return true;
}

//点击密码框
function onblursCheckPsw(obj) {
    obj = $(obj).closest(".login_containter");
    $(obj).find(".errorRegPwd").hide();
    var regPwd = $("input[name='regpsw']").val();
    if (regPwd == "") {
        $(obj).find(".errorRegPwd.error_info").html("<i></i>请输入密码").show().addClass('TxtShadow').css("right","-179px");
        $(obj).find(".errorRegPwd.error_info").parent().addClass('input_item_error');
        return false;
    }
    else if (!(/^.{6,}$/.test(regPwd))) {
        $(obj).find(".errorRegPwd.error_info").html("<i></i>请输入至少6位任意字符的密码").show().addClass('TxtShadow').css("right","-280px");
        $(obj).find(".errorRegPwd.error_info").parent().addClass('input_item_error');
        return false;
    }
    psw_check = true;
    return true;
}
//点击密码框
function onblursCheckPswPP(obj) {
    obj = $(obj).closest(".login_containter");
    $(obj).find(".errorRegPwdPP").hide();
    var regPwd = $("input[name='newpsw']").val();
    if (regPwd == "") {
        $(obj).find(".errorRegPwdPP.error_info").html("<i></i>请输入密码").show().addClass('TxtShadow').css("right","-179px");
        $(obj).find(".errorRegPwdPP.error_info").parent().addClass('input_item_error');
        return false;
    }
    else if (!(/^.{6,}$/.test(regPwd))) {
        $(obj).find(".errorRegPwdPP.error_info").html("<i></i>请输入至少6位任意字符的密码").show().addClass('TxtShadow').css("right","-280px");
        $(obj).find(".errorRegPwdPP.error_info").parent().addClass('input_item_error');
        return false;
    }
    psw_checkpp = true;
    return true;
}

function onKeyupSend(obj){
    obj = $(obj).closest(".login_containter");
    var $loginPhone = $("input[name='userEmail']");
    var loginPhone =$loginPhone.val().replace(/\s+/g, "").replace($loginPhone.attr("eg"), "");
    if (!(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(loginPhone))) {
        $('.goSend').removeClass('canClick').addClass('zhihui');
    } else {
        $(obj).find(".error_info").hide();
        //邮箱验证成功后，即可点击发送邮件验证码
        $('.goSend').removeClass('zhihui').addClass('canClick');
    }
}

function onKeyupSendPP(obj){
    obj = $(obj).closest(".login_containter");
    var $loginPhone = $("input[name='userEmailPsw']");
    var loginPhone =$loginPhone.val().replace(/\s+/g, "").replace($loginPhone.attr("eg"), "");
    if (!(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(loginPhone))) {
        $('.goSend').removeClass('canClickPP').addClass('zhihui');
    } else {
        $(obj).find(".error_info").hide();
        //邮箱验证成功后，即可点击发送邮件验证码
        $('.goSend').removeClass('zhihui').addClass('canClickPP');
    }
}

//发送邮箱验证码
$(document).on("click", ".canClick", function () {
    if (!email_check) {
        $(".errorloginPhone.error_info").html("<i></i>此邮箱已注册").show().addClass('TxtShadow').css("right","-202px");
        $(".errorloginPhone.error_info").parent().addClass('input_item_error');
        return;
    }
    var email = $(this).parent().find('input[name="userEmail"]').val();
    if (onblursLoginPhone($('input[name="userEmail"]'))) {
        settime();
        jsonAjaxJsonp("GET", u+"/mail/send", "usermail="+email+"&type=0", function (data) {
            if (data.errorCode != '0') {
                $(this).closest(".login_containter").find(".errorMailPwd").html("<i></i>抱歉，发送失败").show().addClass('TxtShadow').css("right","-179px");
            }
        });
    }
});
//发送邮箱验证码
$(document).on("click", ".canClickPP", function () {
    if (!email_checkpp) {
        $(".errorloginPhonePP.error_info").html("<i></i>此邮箱未注册").show().addClass('TxtShadow').css("right","-202px");
        $(".errorloginPhonePP.error_info").parent().addClass('input_item_error');
        return;
    }
    var email = $(this).parent().find('input[name="userEmailPsw"]').val();
    if (onblursLoginPhonePP($('input[name="userEmailPsw"]'))) {
        settimep();
        jsonAjaxJsonp("GET", u+"/mail/send", "usermail="+email+"&type=1", function (data) {
            if (data.errorCode != '0') {
                $(this).closest(".login_containter").find(".errorMailPwd").html("<i></i>抱歉，发送失败").show().addClass('TxtShadow').css("right","-179px");
            }
        });
    }
});
// 更改密码
var LOCK_changePswSubmit = false;
function ChangePswSubmit(obj) {
    if (LOCK_changePswSubmit) {
        return;
    }
    if (!email_checkpp) {
        onblursLoginPhonePP($('input[name="userEmailPsw"]'));
    }
    if (!code_checkpp) {
        onblursCheckCodePP($('input[name="codevalPsw"]'));
    }
    if (!psw_checkpp) {
        onblursCheckPswPP($('input[name="newpsw"]'));
    }
    if (email_checkpp && psw_checkpp && code_checkpp) {
        LOCK_changePswSubmit = true;
        var usermail = $('input[name="userEmailPsw"]').val();
        var code = $('input[name="codevalPsw"]').val();
        var password = $('input[name="newpsw"]').val();
        jsonAjaxJsonp("GET", u+"/mail/changepsw", "usermail="+usermail+"&password="+password+"&code="+code, function (data) {
            if (data.errorCode == '0') {
                $('#confirmChange').attr('style', "cursor:not-allowed;background-color:#6666").attr('disabled', true);;
                $('#confirmChange').val('修改密码ing');
                setTimeout(function (){
                    regsuccess('修改密码');
                },2000);
            } else {
                LOCK_changePswSubmit = false;
                console.log('修改密码失败');
            }
        });
    }
    return;
}

// 邮箱注册
var LOCK_regMailSubmit = false;
function RegMailSubmit(obj){
    if (LOCK_regMailSubmit) {
        return;
    }
    obj = $(obj).closest(".login_containter");
    if (!username_check) {
        onblursCheckName($('input[name="username"]'));
    }
    if (!email_check) {
        onblursLoginPhone($('input[name="userEmail"]'));
    }
    if (!code_check) {
        onblursCheckCode($('input[name="codeval"]'));
    }
    if (!psw_check) {
        onblursCheckPsw($('input[name="regpsw"]'));
    }
    if (username_check && email_check && psw_check && code_check) {
        LOCK_regMailSubmit = true;
        var username = $('input[name="username"]').val();
        var usermail = $('input[name="userEmail"]').val();
        var code = $('input[name="codeval"]').val();
        var password = $('input[name="regpsw"]').val();
        jsonAjaxJsonp("GET", u+"/mail/register", "usermail="+usermail+"&username="+username+"&password="+password+"&code="+code, function (data) {
            if (data.errorCode == '0') {
                $('.MailRegBtn').attr('style', "cursor:not-allowed;background-color:#6666").attr('disabled', true);;
                $('.MailRegBtn').val('注册中...');
                setTimeout(function (){
                    regsuccess('注册');
                },2000);
            } else {
                LOCK_regMailSubmit = false;
                console.log('注册失败');
            }
        });
    }
    return;
}
var cdown = 3;
function regsuccess(v){
    var btn = $('.MailRegBtn');
    if (cdown == 0) {
        $.cookie("LoginName", null, { path: "/", domain: _Domain });
        $.cookie("token", null, { path: "/", domain: _Domain });
        location.href = decodeURIComponent(redirect_u + "/login.html");
        return;
    } else {
        btn.val(v+"成功，前去登录(" + cdown + ")");
        cdown--;
    }
    setTimeout(regsuccess(v), 1000);
}

var countdown = 60;
function settime(){
    var btn = $('.goSend');
    if (countdown == 0) {
        btn.removeClass("zhihui").addClass('canClick');
        btn.html("点击发送");
        countdown = 60;
        return;
    } else {
        btn.removeClass('canClick').addClass('zhihui');
        btn.html("重新发送(" + countdown + ")");
        countdown--;
    }
    setTimeout(settime, 1000);
}

var countdown1 = 60;
function settimep(){
    var btn= $('.changePsw');
    if (countdown == 0) {
        btn.removeClass("zhihui").addClass('canClickPP');
        btn.html("点击发送");
        countdown = 60;
        return;
    } else {
        btn.removeClass('canClickPP').addClass('zhihui');
        btn.html("重新发送(" + countdown1 + ")");
        countdown1--;
    }
    setTimeout(settimep, 1000);
}

function CheckLoginPhone(obj) {
    $(obj).find(".error_info").hide();
     
    if (typeof obj== "undefined") {
        var $loginPhone = $("input[name='userEmail']");
        var loginPhone =$loginPhone.val().replace(/\s+/g, "").replace($loginPhone.attr("eg"), "");
        var $loginCode = $(".loginCode");
        var loginCode = $loginCode.val().replace(/\s+/g, "").replace($loginCode.attr("eg"), "");

        if (loginPhone == ""&&loginCode == "") {
           $(obj).find(".errorloginPhone.error_info").html("<i></i>请输入手机号和短信验证码").show().addClass('TxtShadow').css("right","-262px");
           $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');

            return false;
        }
        else  if (loginPhone == "") {
           $(obj).find(".errorloginPhone.error_info").html("<i></i>请输入手机号").show().addClass('TxtShadow').css("right","-190px");
           $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');
            $("input[name='userEmail']").focus();
            return false;
        }
        else if (!/^1(3|4|5|7|8)\d{9}$/.test(loginPhone)) {
            $(obj).find(".errorloginPhone.error_info").html("<i></i>请输入正确的11位手机号码").show().addClass('TxtShadow').css("right","-265px");
            $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');
             $("input[name='userEmail']").focus();
        return false;
        } 
        else if (loginCode == "") {
            $(obj).find(".errorloginCode.error_info").html("<i></i>请输入短信验证码").show().addClass('TxtShadow').css("right","-215px");
            $(obj).find(".errorloginCode.error_info").parent().addClass('input_item_error');
            $(".loginCode").focus();
            return false;
        } 
        else{
             $(obj).find(".input_item").removeClass('input_item_error');
             return true;
        }
    }
    else{
        var $loginPhone = $("input[name='userEmail']");
        var loginPhone =$loginPhone.val().replace(/\s+/g, "").replace($loginPhone.attr("eg"), "");
        var $loginCode = $(".loginCode");
        var loginCode = $loginCode.val().replace(/\s+/g, "").replace($loginCode.attr("eg"), "");

        if (loginPhone == ""&&loginCode == "") {
           $(obj).find(".errorloginPhone.error_info").html("<i></i>请输入手机号和短信验证码").show().addClass('TxtShadow').css("right","-262px");
           $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');
            return false;
        }
        else  if (loginPhone == "") {
           $(obj).find(".errorloginPhone.error_info").html("<i></i>请输入手机号").show().addClass('TxtShadow').css("right","-190px");

           $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');
           $("input[name='userEmail']").focus();
            return false;
        }
        else if (!/^1(3|4|5|7|8)\d{9}$/.test(loginPhone)) {
            $(obj).find(".errorloginPhone.error_info").html("<i></i>请输入正确的11位手机号码").show().addClass('TxtShadow').css("right","-265px");
            $(obj).find(".errorloginPhone.error_info").parent().addClass('input_item_error');
            $("input[name='userEmail']").focus();
            return false;
        } 
        else if (loginCode == "") {
            $(obj).find(".errorloginCode.error_info").html("<i></i>请输入短信验证码").show().addClass('TxtShadow').css("right","-215px");
            $(obj).find(".errorloginCode.error_info").parent().addClass('input_item_error');
           $(".loginCode").focus();
            return false;
        } 
        else{
            $(".errorloginPhone.error_info").parent().removeClass('input_item_error');
             return true;
        }
    }
}

/*******************************快捷登录**********************************/
    var commonappDialog, commonbindDialog, loginUrl,indexUrl;

    //三方登录
    function openThirdParty(type, obj) {
        var thirdUrl = u + "/"+type+"/login";
        var qrid = $(obj).data('qrid');
        var appplt = $(obj).data('appplt');
        if (typeof qrid != 'undefined') {
            thirdUrl += ("?qrid="+qrid+"&appplt="+appplt);
        }
        A.jsonpJqueryAjax(thirdUrl, function (data) {
            location.href = data.authurl;
        })
    }

function getUrlParam(name){  
    //构造一个含有目标参数的正则表达式对象  
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");  
    //匹配目标参数  
    var r = window.location.search.substr(1).match(reg);  
    //返回参数值  
    if (r!=null) return unescape(r[2]);  
    return null;  
}  

$(document).ready(function(){
// 
//初始获取COOKIE
    if ($.cookie("LoginName") != null) {
        LoginName = unescape($.cookie("LoginName").replace(/\\/g, "%"));
    }
   // LoginPhone = $.cookie("LoginPhone");
    //在当前的文本框赋值
    if (typeof LoginName != "undefined") {
        $("input[name='loginname']").val(LoginName);
    }
    // if (typeof LoginPhone != "undefined") {
    //     $("input[name='userEmail']").val(LoginPhone);
    // }
     if ($.cookie("uid") != null) {
     // location.href="http://www.yifutu.com";  
        location.href=indexUrl;
      
    }
   
    // 用户输入三次用户密码错误时出现
    if ($.cookie("eTime") == null||$.cookie("eTime") == undefined) {
        $(".input_item.input_item_yzm").hide();
    }
    else{
       $(".input_item.input_item_yzm").show();
    } 
     if($.cookie("eTime") == 3)
    {
        $(".input_item.input_item_yzm").show();
         $(".input_item.input_item_yzm").find(".loginCapt").attr('src', '/Ajax/GetValidateCode?').click();
    }

       // $("#hid_loginRefurl").val(location.href);
       if(getUrlParam('returnurl')!=null){
        var returnURL = getUrlParam('returnurl');
        if (returnURL.lastIndexOf("http://login.yifutu.com/") == returnURL.length - 1) {
            returnURL = returnURL.substring(0, returnURL.lastIndexOf("http://login.yifutu.com/"));
        }
        $("#hid_loginRefurl").val(returnURL);    
}
$(document).on("click",".login_change a",function(){
    if(returnURL!=undefined){
        $(this).attr('href', LoginUrl+'LoginReg/Reg?returnurl='+$("#hid_loginRefurl").val());
    }
    else{
        $(this).attr('href', LoginUrl+'LoginReg/Reg');
    }
    
});





});

 // //页面关闭
 //    $(window).bind('beforeunload', function () {
 //        dataCommon.SaveLoginOut();
 //    });
/***************回车提交*******************/
function KeyDownFunction(fun, obj) {
    LOCK_LoginFormSubmit = false;
    LOCK_PhoneFormSubmit = false;
    var event = arguments.callee.caller.arguments[0] || window.event; //消除浏览器差异  
    if (event.keyCode == 13) {
        fun(obj);
    }
}
/***************回车提交 END*******************/
$(document).on('keydown', 'input.loginName', function() {
  KeyDownFunction(LoginFormSubmit,$(this));
});
$(document).on('keydown', 'input.loginPwd', function() {
  KeyDownFunction(LoginFormSubmit,$(this));
});
$(document).on('keydown', 'input.loginyzm', function() {
  KeyDownFunction(LoginFormSubmit,$(this));
});

$(function(){
    var t = UrlParm.parm("t");
    if (t==0 && typeof t !== 'undefined') {
        $('.goReg').click();
    }
});