var $backToTopEle=$('<a href="javascript:void(0)" class="Hui-iconfont toTop" title="返回顶部" alt="返回顶部" style="display:none">^^</a>').appendTo($("body")).click(function(){
	$("html, body").animate({ scrollTop: 0 }, 120);
});
var backToTopFun = function() {
	var st = $(document).scrollTop(), winh = $(window).height();
	(st > 0)? $backToTopEle.show(): $backToTopEle.hide();
	/*IE6下的定位*/
	if(!window.XMLHttpRequest){
		$backToTopEle.css("top", st + winh - 166);
	}
};
	$(function(){
		$(window).on("scroll",backToTopFun);
		backToTopFun();
	});

var  _Domain = document.domain;
var  _SQDomain = '.yangshuqian.com';

UrlParm = function () { // url参数
    var data, index;
    (function init() {
        data = [];
        index = {};
        var u = window.location.search.substr(1);
        if (u != '') {
            var parms = decodeURIComponent(u).split('&');
            for (var i = 0, len = parms.length; i < len; i++) {
                if (parms[i] != '') {
                    var p = parms[i].split("=");
                    if (p.length == 1 || (p.length == 2 && p[1] == '')) {// p | p=
                        data.push(['']);
                        index[p[0]] = data.length - 1;
                    } else if (typeof(p[0]) == 'undefined' || p[0] == '') { // =c | =
                        data[0] = [p[1]];
                    } else if (typeof(index[p[0]]) == 'undefined') { // c=aaa
                        data.push([p[1]]);
                        index[p[0]] = data.length - 1;
                    } else {// c=aaa
                        data[index[p[0]]].push(p[1]);
                    }
                }
            }
        }
    })();
    return {
        // 获得参数,类似request.getParameter()
        parm: function (o) { // o: 参数名或者参数次序
            try {
                return (typeof(o) == 'number' ? data[o][0] : data[index[o]][0]);
            } catch (e) {
            }
        },
        //获得参数组, 类似request.getParameterValues()
        parmValues: function (o) { // o: 参数名或者参数次序
            try {
                return (typeof(o) == 'number' ? data[o] : data[index[o]]);
            } catch (e) {
            }
        },
        //是否含有parmName参数
        hasParm: function (parmName) {
            return typeof(parmName) == 'string' ? typeof(index[parmName]) != 'undefined' : false;
        },
        // 获得参数Map ,类似request.getParameterMap()
        parmMap: function () {
            var map = {};
            try {
                for (var p in index) {
                    map[p] = data[index[p]];
                }
            } catch (e) {
            }
            return map;
        }
    }
}();

var u = "http://127.0.0.1:21862/chlsq/api";
// var u = "http://127.0.0.1/chlsq/api";

var redirect_u = "http://localhost:63342/sqblog/qian";
// var redirect_u = "http://www.yangshuqian.com";

//校验
var V = {
    isNAN: function (p, m) {
        if (p == null || p == '' || typeof p === 'undefined') {
            console.error('Error loading ' + m);
            return false;
        }
        return true;
    },
    isNaN: function (p) {
        return (p == null || p == '' || typeof p === 'undefined')?false:true;
    },
    isFunction: function (f) {
        return typeof f === 'function';
    },
    isN: function (p) {
        if (p == null || p == '' || typeof p === 'undefined') {
            return "无";
        }
        return p;
    }
}

$(function(){

    $('#search-btn').click(function () {
        var search = $('#search-content').val();
        if (search=='' || typeof search == 'undefined' || search==null) return
        location.href="article_tag.html?search="+search;
    });

    var loginName = $.cookie("LoginName");
    if (loginName==null) {
        $('#login-no').show();
        $('#login-in').hide();
    } else {
        $('#login-in').show();
        $('#login-no').hide();
    }

    $('.login-reg').click(function(){
        var type = $(this).data('id');
        if ('login' == type) {
            location.href = "login.html";
        }
        if('reg' == type){
            location.href = "login.html?t=0";
        }
        return
    })

    $('.qrcode-position').click(function () {
        var appplt = browserOnlyId();
        var qrid_url = u+'/qrcode/getQrid?appplt='+appplt;
        var qrcode_url = u+'/qrcode/getQrcode?size=250&qrid=';
        var polling_url = u+'/qrcode/polling?appplt='+appplt+'&qrid=';
        A.jsonpJqueryAjax(qrid_url, function (qridRes) {
            if (qridRes.errorCode != '0') {
                layer.msg(qridRes.message);
                return;
            }
            var current_qrid = qridRes.qrid;
            qrcode_url += current_qrid;
            var qrtimer;
            layer.open({
                type: 2,
                title: '扫码登录',
                closeBtn: 0,
                area: ['230px', '273px'],
                offset: '160px',
                anim: 2,
                id:'qrcodeLayer',
                scrollbar: false,
                shadeClose: true,
                content: [qrcode_url, 'no'],
                success: function(){
                    $('iframe[id^="layui-layer-iframe"]').attr('style', 'height:230px');
                    //轮训二维码状态
                    polling_url += current_qrid;
                    qrtimer = setInterval(function () {
                        getQrcodeStatus(polling_url, qrtimer);
                    },2000);
                },
                end: function () {
                    clearInterval(qrtimer);
                }
            });
        })
    });
});

function getQrcodeStatus(polling_url, qrtimer) {
    A.jsonpJqueryAjax(polling_url, function (res) {
        if (res.status == '2') {
            clearInterval(qrtimer);
            $('.layui-layer-title').text("扫描成功，正在登录...");
            setTimeout(function () {
                A.saveUserCookie(res, true);
            }, 3000);
            return;
        }
    });
}

// 工具
var A = {
    withoutHtml: function (o) {
        return o.replace(/<[\\/\\!]*[^<>]*>/ig,"");
    },
    jsonpJqAjax: function(url, fun, f){
        if(!V.isNaN(url) && V.isFunction(f)){
            f();
        }
        $.ajax({
            url: url,
            type: 'get',
            dataType: 'jsonp',
            jsonpCallback :"cb",
            contentType: false,
            processData: false,
            cache: false,
            success: function(data){
                fun(data);
                if (V.isFunction(f)) {
                    f();
                }
            },
            error: function (e) {
                throw new Error('jsonp ajax request occurs error.'+JSON.stringify(e));
            }
        });
    },
    jsonpJqueryAjax: function(url, f){
        $.ajax({
            url: url,
            type: 'get',
            dataType: 'jsonp',
            jsonpCallback :"cb",
            contentType: false,
            processData: false,
            cache: false,
            success: function(data){
                f(data);
            },
            error: function (e) {
                throw new Error('jsonp ajax request occurs error.'+JSON.stringify(e));
            }
        });
    },
    siteArticleHTML: function(data){
        if(!V.isNAN(data, 'siteArticleHTML is null')){
            return;
        }
        $('.left-release').empty();
        $.each(data, function (k, v) {
            $('.left-release').append('<li><a href="article_detail.html?id='+v.articleId+'">'+v.articleTitle+'</a></li>');
        });
    },
    friendShipLinkHTML: function (data) {
        if(!V.isNAN(data, 'friendShipLinkHTML is null')){
            return;
        }

        $('.shiplink').empty();

        $.each(data, function (k,v) {
            $('.shiplink').append('<li><a href="'+v.link+'" target="_blank">'+v.name+'</a></li>');
        });

    },
    switchBarClass: function(id){
        $('a[id^="bar"]').removeClass('active');
        $('#bar'+id).addClass('active');
    },
    initHeadPic: function () {
        if (V.isNaN($.cookie('headpic'))) {
            $('.img-circle').attr('src', $.cookie('headpic'));
        }
    },
    goToLogin: function (p) {
        var msg = '请先登录再进行点赞哦';
        if (V.isNaN(p)) {
            msg = p;
        }
        layer.confirm('<b style="font-size: 14px;font-family: \'Microsoft Yahei\'">'+msg+'</b>', {
            btn: ['前去登录'], //按钮
        }, function(){
            location.href = decodeURIComponent("http://www.yangshuqian.com/login.html");
        });
    },
    paramToken: function (param) {
        var username = $.cookie('LoginName');
        var token = $.cookie('token');
        return param + "&username="+username+"&token="+token;
    },
    tagPosition: function () {
        var str = location.href
        var num = str.indexOf("#");
        if (num != -1) {
            str = str.substr(num + 1);
            return str;
        }
        return null;
    },
    goTo : function(target){
        var scrollT = document.body.scrollTop|| document.documentElement.scrollTop
        if (scrollT >target) {
            var timer = setInterval(function(){
                var scrollT = document.body.scrollTop|| document.documentElement.scrollTop
                var step = Math.floor(-scrollT/6);
                document.documentElement.scrollTop = document.body.scrollTop = step + scrollT;
                var scrollF = document.body.scrollTop|| document.documentElement.scrollTop
                if (scrollF == scrollT) {
                    clearTimeout(timer);
                }
                if(scrollT <= target){
                    document.body.scrollTop = document.documentElement.scrollTop = target;
                    clearTimeout(timer);
                }
            },20)
        }else if(scrollT == 0){
            var timer = setInterval(function(){
                var scrollT = document.body.scrollTop|| document.documentElement.scrollTop
                var step = Math.floor(300/3*0.7);
                document.documentElement.scrollTop = document.body.scrollTop = step + scrollT;
                var scrollF = document.body.scrollTop|| document.documentElement.scrollTop
                if (scrollF == scrollT) {
                    clearTimeout(timer);
                }
                if(scrollT >= (target-500)){
                    document.body.scrollTop = document.documentElement.scrollTop = target-500;
                    clearTimeout(timer);
                }
            },20)
        }else if(scrollT < target){
            var timer = setInterval(function(){
                var scrollT = document.body.scrollTop|| document.documentElement.scrollTop
                var step = Math.floor(scrollT/6);
                document.documentElement.scrollTop = document.body.scrollTop = step + scrollT;
                var scrollF = document.body.scrollTop|| document.documentElement.scrollTop
                if (scrollF == scrollT) {
                    clearTimeout(timer);
                }
                if(scrollT >= (target-500)){
                    document.body.scrollTop = document.documentElement.scrollTop = (target-500);
                    clearTimeout(timer);
                }
            },20)
        }else if(target == scrollT){
            return false;
        }
    },
    saveUserCookie: function (data, gohome) {
        cleanCookie();
        $.cookie("LoginName", data.userinfo.username, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });
        $.cookie("token", data.token, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });
        $.cookie("nickname", data.userinfo.nickname, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });
        $.cookie("headpic", data.userinfo.headpic, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });
        $.cookie("phone", data.userinfo.phone, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });
        $.cookie("email", data.userinfo.email, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });
        $.cookie("userId", data.userinfo.userId, { expires: 7, domain: _Domain, path: '/', encode: true, encodeFun: "encodeURI" });

        if (gohome) {
            //返回上一个页面的地址
            location.href = decodeURIComponent(redirect_u);
            window.event.returnValue = false;
        }
    }
}

//cookie 读写、转码
jQuery.cookie = function (name, value, options) {
    if (typeof value != 'undefined') {
        options = options || {};
        if (value === null) {
            value = '';
            options = $.extend({}, options);
            options.expires = -1;
        }
        var expires = '';
        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
            var date;
            if (typeof options.expires == 'number') {
                date = new Date();
                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
            } else {
                date = options.expires;
            }
            expires = '; expires=' + date.toUTCString();
        }
        var path = options.path ? '; path=' + (options.path) : '';
        var domain = options.domain ? '; domain=' + (options.domain) : '';
        var secure = options.secure ? '; secure' : '';
        if (typeof options.encode == 'undefined') {
            options.encode = true;
            options.encodeFun = "encodeURIComponent";
        }
        if (options.encode) {
            if (options.encodeFun == "escape") {
                value = escape(value);
            } else if (options.encodeFun == "encodeURI") {
                value = encodeURI(value);
            } else if (options.encodeFun == "enUnicode") {
                value = escape(value).replace(/%/g, "\\").toLowerCase();
            } else {
                value = encodeURIComponent(value);
            }
        }
        document.cookie = [name, '=', value, expires, path, domain, secure].join('');
    } else {
        var cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = cookie.substring(name.length + 1);

                    cookieValue = decodeURIComponent(cookieValue);

                    break;
                }
            }
        }
    }
    return cookieValue;
};

function jsonAjaxJsonp(type, url, param, success, error) {
    $.ajax({
        async: true,
        type: type,
        url: url,
        data: param,
        cache: false,
        dataType: "jsonp",
        crossDomain: true,//同域请求为false，跨域请求为true，如果你想强制跨域请求（如JSONP形式）同一域，设置crossDomain为true。这使得例如，服务器端重定向到另一个域
        jsonpCallback :"cb",
        contentType: false,
        processData: false,
        success: function (data) {
            success(data);
        },
        error: function (data) {
            if (typeof data != "undefined" && typeof error != "undefined") {
                error(data);
            }
        }
    });
}
function jsonAjaxJsonpSync(type, url, param, success, error) {
    $.ajax({
        async: false,
        type: type,
        url: url,
        data: param,
        cache: false,
        dataType: "jsonp",
        crossDomain: true,//同域请求为false，跨域请求为true，如果你想强制跨域请求（如JSONP形式）同一域，设置crossDomain为true。这使得例如，服务器端重定向到另一个域
        jsonpCallback :"cb",
        contentType: false,
        processData: false,
        success: function (data) {
            success(data);
        },
        error: function (data) {
            if (typeof data != "undefined" && typeof error != "undefined") {
                error(data);
            }
        }
    });
}

function addComment(id, uid, fuid, cid, content, fun) {
    var param = "articleId="+id+"&userId="+uid+"&content="+content+(cid==null?"":"&commentId="+cid)+(fuid==null?"":"&foruserId="+fuid);
    jsonAjaxJsonp("GET", u+"/article/comment/add", A.paramToken(param), function (data) {
        if (data.errorCode != '0') {
            layer.msg(data.message);
            return
        } else {
            fun();
        }
    })
}

function logout(){
    cleanCookie();
    var cg = A.tagPosition();
    if (cg!=null) {
        location.href = location.href.replace('#'+cg, '');
    } else {
        location.reload();
    }
}

function cleanCookie() {
    $.cookie("LoginName", null, {path: "/", domain: _SQDomain});
    $.cookie("token", null, {path: "/", domain: _SQDomain});
    $.cookie("nickname", null, {path: "/", domain: _SQDomain});
    $.cookie("phone", null, {path: "/", domain: _SQDomain});
    $.cookie("headpic", null, {path: "/", domain: _SQDomain});
    $.cookie("email", null, {path: "/", domain: _SQDomain});
    $.cookie("userId", null, {path: "/", domain: _SQDomain});
    $.cookie("LoginName", null, {path: "/", domain: _Domain});
    $.cookie("token", null, {path: "/", domain: _Domain});
    $.cookie("nickname", null, {path: "/", domain: _Domain});
    $.cookie("phone", null, {path: "/", domain: _Domain});
    $.cookie("headpic", null, {path: "/", domain: _Domain});
    $.cookie("email", null, {path: "/", domain: _Domain});
    $.cookie("userId", null, {path: "/", domain: _Domain});
}

function browserOnlyId() {
    var canvas = document.createElement('canvas');
    var ctx = canvas.getContext('2d');
    var txt = 'http://security.tencent.com/';
    ctx.textBaseline = "top";
    ctx.font = "14px 'Arial'";
    ctx.textBaseline = "tencent";
    ctx.fillStyle = "#f60";
    ctx.fillRect(125, 1, 62, 20);
    ctx.fillStyle = "#069";
    ctx.fillText(txt, 2, 15);
    ctx.fillStyle = "rgba(102, 204, 0, 0.7)";
    ctx.fillText(txt, 4, 17);

    var b64 = canvas.toDataURL().replace("data:image/png;base64,", "");
    var bin = atob(b64);
    var crc = bin2hex(bin.slice(-16, -12));
    //var crc = bin.slice(-16,-12);
    return crc;
}

function bin2hex(str) {
    var result = "";
    for (i = 0; i < str.length; i++) {
        result += int16_to_hex(str.charCodeAt(i));
    }
    return result;
}

function int16_to_hex(i) {
    var result = i.toString(16);
    var j = 0;
    while (j + result.length < 4) {
        result = "0" + result;
        j++;
    }
    return result;
}
