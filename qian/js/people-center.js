var LoginName;
var token;
$(function () {
    $('#headForm').attr('action', u+"/upload/pic");
    A.initHeadPic();
    LoginName = $.cookie("LoginName");

    if (!V.isNaN(LoginName)) {
        location.href = "http://www.yangshuqian.com";
        return
    }

    token = $.cookie("token");
    $('.p-center div:eq(0) span').hover(function(){
        $('.p-center div:eq(0) span').attr("style", "color:#00000");
        $(this).attr("style", "color:#619d21;border-radius:2px;font-weight:bold;");
    },function(){
        $(this).attr('style', '');
    });

    $('.p-center div:eq(0) span').click(function () {
        $('.p-center div:eq(0) span').removeClass('menu-selected');
        $(this).addClass('menu-selected');

        $('.p-right div').hide();
        var tab = $(this).data("id");
        $('#content-'+tab).show();
    });

    init();
});

function goComment(obj) {
    var articleId = $(obj).data('id');
    var replyId = $(obj).data('reply-id');
    location.href = redirect_u + '/article_detail.html?id='+articleId+'#c'+replyId;
}

function init() {
    $('#u-nickname').html(V.isN($.cookie("nickname")));
    $('#u-username').html(V.isN(LoginName));
    $('#u-phone').html(V.isN($.cookie("phone")));
    $('#u-email').html(V.isN($.cookie("email")));
    $('img[alt="Logo"]').attr('src', $.cookie("headpic"))

    $('#nickname').val($.cookie("nickname"));
    $('#phone').val($.cookie("phone"));
    $('#oldfile').val($.cookie("headpic"));

    $('input[name="token"]').val(token);
    $('input[name="username"]').val(LoginName);


    var commentListURL = u+'/article/comment/center?userId='+ $.cookie('userId');
    A.jsonpJqueryAjax(commentListURL, function (data) {
        if (data.errorCode!='0') {
            layer.msg(data.message);
        } else {
            if(data.result.length > 0){
                $('#content-2 > span').remove();
                $('#content-2 .zwpl').hide();
                var htmlRes = '';
                $.each(data.result, function (k, v) {
                    htmlRes += '<span><img src="'+v.headpic+'" alt="images"/><span><span>'+v.nickname+'@你:</span><span>'+v.commentContent+'</span>' +
                        '<span><i>'+v.commentDate+'</i><span title="回复" onclick="goComment(this)" data-id="'+v.articleId+'" data-reply-id="'+v.commentId+'">' +
                        '<i class="glyphicon glyphicon-comment" style="color: #ca9387;"></i>  回复</span></span></span></span>';
                });
                $('#content-2').append(htmlRes);
            } else {
                $('#content-2 .zwpl').show();
            }
        }
    })
}

var lock = false;
function saveUserInfo() {
    if(lock){
        return;
    }
    var nickname = $.trim($('#content-1').find('#nickname').val());
    var phone = $.trim($('#content-1').find('#phone').val());

    if (!V.isNaN(nickname)) {
        layer.msg('昵称不能为空哦');return;
    }
    var re =/^[\w\u4e00-\u9fa5]{4,30}$/;
    if (!re.test(nickname)) {
        layer.msg('昵称非法，不能少于4个字符哦');return;
    }
    if (V.isNaN(phone)) {
        if (!(/^1[3456789]\d{9}$/.test(phone))){
            layer.msg('手机号非法');return;
        }
    }
    lock = true;
    var getUrl = u+"/update/user/info?username="+LoginName+"&nickname="+nickname+"&phone="+phone+"&token="+token;

    A.jsonpJqueryAjax(getUrl, function (data) {
        if (data.errorCode=='0') {
            layer.msg('保存成功',{icon: 6}, function(){
                $.cookie("nickname", nickname, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });
                $.cookie("phone", phone, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });
                location.href = decodeURIComponent("http://www.yangshuqian.com");
            });
        } else if (data.errorCode=='5') {
            lock = false;
            layer.msg('token过期，请重新登录');return;
        } else {
            lock = false;
            layer.msg('参数为空');return;
        }
    })
}

