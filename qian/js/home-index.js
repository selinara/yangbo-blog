var dianLike=function(id) {

    if(!V.isNaN($.cookie('LoginName'))){
        A.goToLogin();

        return
    }

    var exist = 'glyphicon-heart';
    var empty = 'glyphicon-heart-empty';
    var status = $('#heart-'+id).attr('data-value');

    var count = parseInt($('#count-'+id).html());
    var rs = empty;
    var as = exist;
    var num = count+1;
    var st = 1;
    if (status!=0) {
        rs = exist;as = empty;num=count-1;st=0;
    }
    $('#heart-'+id).removeClass(rs).addClass(as);
    $('#heart-'+id).attr("data-value", st);
    $('#count-'+id).html(num<0?0:num);

    $.ajax({
        url: u+"/article/like?aid="+id+'&status='+st+'&username='+$.cookie('LoginName')+'&token='+$.cookie('token'),
        type: 'get',
        dataType: 'jsonp',
        jsonpCallback :"cb",
        contentType: false,
        processData: false,
        cache: false,
        success: function (data) {
            if (data.errorCode!='0') {
                layer.msg(data.message);
            }
        }
    });
}
//加载函数
var F = {
    rotationOHTML: function (data) {
        if(!V.isNAN(data, 'rotaition is null')){
            return;
        }

        var carousel = $('#carousel').carousel();

        $.each(data, function(k, v){

            if (k==0){
                $('.carousel-inner').append('<div class="item active"><img src="'+v.url+'" alt="...">'  +
                    '<div class="carousel-caption"><h3></h3><p>'+v.content+'</p></div></div>');
                $('.sitetip h3').text(v.siteTitle);
                $('.sitetip p').text(v.siteSource);
            } else{
                $('.carousel-inner').append('<div class="item"><img src="'+v.url+'" alt="...">'  +
                    '<div class="carousel-caption"><h3></h3><p>'+v.content+'</p></div></div>');
            }
        });

        carousel.add();
    },
    hotTagsHTML: function (data) {
        if(!V.isNAN(data, 'hottags is null')){
            return;
        }
        $('.labelList').empty();

        $.each(data, function(k, v){
            $('.labelList').append('<a class="label label-default" href="article_tag.html?id='+v.labelId+'">'+v.labelName+'</a>');
        });
    },
    articleUpToTop: function(data){
        if(!V.isNAN(data, 'articleUpToTop is null')){
            return;
        }
        $('.contenttop').empty().append('<a href="article_detail.html?id='+data.articleId+'"><strong>博主置顶</strong>' +
            '<h3 class="title">'+data.articleTitle+'</h3>' +
            '<p class="overView" style="overflow:hidden; text-overflow:ellipsis; display:-webkit-box; -webkit-box-orient:vertical; -webkit-line-clamp:3;">'
            +A.withoutHtml(data.articleContent)+'</p></a>');
    },
    homeAriticleHTML: function(data){
        if(!V.isNAN(data, 'homeAriticleHTML is null')){
            return;
        }

        $('.contentList').empty();

        $.each(data, function(k, v){
            var lableHtml = '';
            var labels = v.labelNames;
            for (var i=0; i<labels.length; i++) {
                lableHtml += '<a class="label label-default">'+labels[i]+'</a>';
            }
            var zan = v.isLike == 1?'<i id="heart-'+v.articleId+'" data-value="'+v.isLike+'" class="glyphicon glyphicon-heart"></i>':
                '<i id="heart-'+v.articleId+'" data-value="'+v.isLike+'" class="glyphicon glyphicon-heart-empty"></i>';
            $('.contentList').append('<div class="panel panel-default"><div class="panel-body">' +
                '<h4><a class="title" href="article_detail.html?id='+v.articleId+'">'+v.articleTitle+'</a></h4><p>'+lableHtml+'</p>' +
                '<p class="overView" style="overflow:hidden; text-overflow:ellipsis; display:-webkit-box; -webkit-box-orient:vertical; -webkit-line-clamp:3;">'
                 + A.withoutHtml(v.articleContent)+'</p><p><span class="count"><i class="glyphicon glyphicon-tag"></i>' +
                '<a href="article.html?sid='+v.sortId+'">'+v.sortName+'</a></span> <span class="count"><i class="glyphicon glyphicon-eye-open"></i>阅读:'+v.articleViews+'</span>' +
                '<span class="count"><i class="glyphicon glyphicon-comment"></i>评论:<a href="#">'+v.articleCommentCount+'</a></span><span class="count">' +
                '<i class="glyphicon glyphicon-time"></i>'+v.articleDate+'</span><span class="count">' +
                '<a href="javascript:dianLike('+v.articleId+')">' + zan +
                '<span style="margin-left: 6px;" id="count-'+v.articleId+'">'+v.articleLikeCount+'</span></a></span></p></div></div>');
        });
    },
}

//异步加载首页
var initHomePage = {
    load: function (data) {
        var o = data.data;
        F.rotationOHTML(o.rotations);
        F.hotTagsHTML(o.hotTags);
        F.articleUpToTop(o.topArticle);
        F.homeAriticleHTML(o.homeArticle);
        A.siteArticleHTML(o.siteArticle);
        A.friendShipLinkHTML(o.friendShipLink);
    },
    init: function () {
        A.initHeadPic();
        var url = u+'/home/content/detail';
        var username = $.cookie('LoginName');
        if (V.isNaN(username)) {
            url = url + "?username="+username;
        }
        A.jsonpJqAjax(url, this.load, function () {
            // console.log('loading over');
        });
    }
}

$(function () {
    A.switchBarClass(1);
    initHomePage.init();
});