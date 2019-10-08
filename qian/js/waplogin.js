$('.aui-show').click(function() {
    var pass_type = $('input.password').attr('type');

    if (pass_type === 'password') {
        $('input.password').attr('type', 'text');
        $('.aui-show').removeClass('operate-eye-open').addClass('operate-eye-close');
    } else {
        $('input.password').attr('type', 'password');
        $('.aui-show').removeClass('operate-eye-close').addClass('operate-eye-open');
    }
});
var qrid = UrlParm.parm("qrid");
var appplt = UrlParm.parm("appplt");

$(function () {
    $('.iconQQ').attr('data-qrid', qrid);
    $('.iconQQ').attr('data-appplt', appplt);
    $('.iconWB').attr('data-qrid', qrid);
    $('.iconWB').attr('data-appplt', appplt);
    var Lock_Login = false;
    $('#login_btu').click(function () {
        if (Lock_Login) {
            return;
        }
        var username = $('#account').val();
        var password = $('#password').val();

        if(!V.isNaN(username)){
            layer.msg("请输入用户名");return;
        }
        if(!V.isNaN(password)){
            layer.msg("请输入密码");return;
        }
        if(!V.isNaN(qrid) || !V.isNaN(appplt)){
            layer.msg("缺少参数");return;
        }
        Lock_Login = true;
        jsonAjaxJsonp("GET", u+"/login", "loginname=" + username + "&password=" + password , function (data) {
            Lock_Login = false;
            if (data.errorCode == '0') {
                A.saveUserCookie(data, false);
                location.href = "/ph-success.html?qrid="+qrid+"&appplt="+appplt;
            } else {
                layer.msg(data.message);
            }
        });
    });
});