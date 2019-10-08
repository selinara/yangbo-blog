var T = {
    loadDetail: function(data){
        var arti = data.data.article;

        $('#likecount').text(arti.articleLikeCount);
        if (arti.isLike == '0') {
            $('#likestatus').removeClass('heartAnimation').attr('rel', 'like');
        } else {
            $('#likestatus').addClass('heartAnimation').attr('rel', 'unlike');
        }
        A.switchBarClass(arti.sortId);
        $('.sonmenu').empty().html('<a href="article.html?sid='+arti.sortId+'" class="sonmenu">'+arti.sortName+'</a>');
        $('.title-arti').empty().text(arti.articleTitle);
        $('.c_titile').empty().text(arti.articleTitle);
        $('.d_time').empty().text("发布时间："+arti.articleDate);
        $('.author').empty().text(arti.author);
        $('.read-num').empty().text("阅读（"+arti.articleViews+"）");
        $('.infos').empty().html(arti.articleContent);
        var labelNames = arti.labelName;
        var lArr = labelNames.split(',');
        for (var i=0; i<lArr.length; i++) {
            $('.crux').append('<a class="label label-default">'+lArr[i]+'</a>');
        }

        // previous - next page
        var prenex = data.data.prenext;
        for(var i=0;i<prenex.length;i++){
            var o = prenex[i];
            if (o.w === '0') {
                $('.last').empty().append('上一篇：<a style="font-family: \'Microsoft Yahei\'" href="article_detail.html?id='+o.id+'">'+o.title+'</a>');
            } else {
                $('.next').empty().append('下一篇：<a style="font-family: \'Microsoft Yahei\'" href="article_detail.html?id='+o.id+'">'+o.title+'</a>');
            }
        }

        // site
        A.siteArticleHTML(data.data.siteArticle);
        A.friendShipLinkHTML(data.data.friendShipLink);
        return
    },
}
function dashangToggle(){
    $(".hide_box").fadeToggle();
    $(".shang_box").fadeToggle();
}

$(function () {
    A.initHeadPic();
    var id = UrlParm.parm("id");
    var url = u+"/article/detail?id="+id;
    if (V.isNaN($.cookie('LoginName'))){
        url = url +"&username="+$.cookie('LoginName');
    }
    A.jsonpJqAjax(url, T.loadDetail, function () {
        jsonAjaxJsonp("GET", u+"/article/comment", "articleId="+id+"&username="+$.cookie('LoginName'), function (data) {
            var arr = data.result;
            $(".comment-list").addCommentList({data:arr,add:""});

            var tag_position = A.tagPosition();
            if (V.isNaN(tag_position)) {
                var offSet = $('#'+tag_position).offset();
                A.goTo(offSet['top']);
                $('#'+tag_position).click();
                setTimeout(function () {$('.mytextarea').focus();  },500);
                return
            }
        })
    });

    $(".pay_item").click(function(){
        $(this).addClass('checked').siblings('.pay_item').removeClass('checked');
        var dataid=$(this).attr('data-id');
        $(".shang_payimg img").attr("src","img/"+dataid+"img.jpg");
        $("#shang_pay_txt").text(dataid=="alipay"?"支付宝":"微信");
    });

    var commentLock = false;
    $("#comment").click(function(){

        if (commentLock) {
            layer.msg('哇哦，手速这么快，等会儿在评论吧');
            return;
        }

        if(!V.isNaN($.cookie('LoginName'))){
            A.goToLogin('请先登录在进行评论哦');
            return
        }

        var content =$("#content").val() ;
        if (!V.isNaN(content)) {
            layer.msg('评论内容不能为空哦');
            return
        }

        commentLock = true;

        addComment(id, $.cookie('userId'), null, null, content, function () {
            $('#comment').addClass('comment-btn-ban');
            var obj = new Object();
            obj.img=$.cookie("headpic");
            obj.replyName=$.cookie('nickname');
            obj.content=content;
            obj.replyBody="";
            obj.likenum = 0;
            obj.uid=$.cookie('userId');
            $(".comment-list").addCommentList({data:[],add:obj});
            $("#content").val('')
            if (!$('.zwpl').is(':hidden')) {
                $('#tagnew').show();
                $('.zwpl').hide();
            }
            setTimeout(function () { commentLock=false;$('#comment').removeClass('comment-btn-ban'); },10000);
        })

    });

    if (V.isNaN($.cookie('LoginName'))) {
        //初始化用户头像
        $('.usernick img').attr('src', $.cookie('headpic'));
        $('.usernick span').html($.cookie('nickname'));
        $('.usernick').show();
    }

    $('body').on("click",'.heart',function(){
        if (!V.isNaN($.cookie('LoginName'))) {
            A.goToLogin();
            return;
        }
        var cc=parseInt($("#likecount").text());
        $(this).css("background-position","")
        var dd=$(this).attr("rel");
        var st;
        if(dd === 'like') {
            $("#likecount").html(cc+1);
            $(this).addClass("heartAnimation").attr("rel","unlike");
            st = 1;
        }
        else{
            $("#likecount").html(cc-1);
            $(this).removeClass("heartAnimation").attr("rel","like");
            $(this).css("background-position","left");
            st = 0;
        }

        $.ajax({
            url: u+"/article/like?aid="+id+'&status='+st+'&token='+$.cookie('token')+'&username='+$.cookie('LoginName'),
            type: 'get',
            dataType: 'jsonp',
            jsonpCallback :"cb",
            contentType: false,
            processData: false,
            cache: false,
        });
    });

});

