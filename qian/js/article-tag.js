var url = u+"/article/list";
var search = UrlParm.parm("search");
var id = UrlParm.parm("id");

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
    $('#count-'+id).html(num);

    $.ajax({
        url: u+"/article/like?aid="+id+'&status='+st+'&token='+$.cookie('token')+'&username='+$.cookie('LoginName'),
        type: 'get',
        dataType: 'jsonp',
        jsonpCallback :"cb",
        contentType: false,
        processData: false,
        cache: false,
    });
}
var getLabelHtml= function(name){
    var lArr = name.split(',');
    var html = '';
    for (var i=0; i<lArr.length; i++) {
        html+='<a class="label label-default">'+lArr[i]+'</a>';
    }
    return html;
}
var L = {

    loadSort: function(data){
        var ar = data.data.homeArticle;
        A.switchBarClass(1)
        $('.sonmenu').empty();

        // 列表
        $('.contentList').empty();

        if(ar.length==0){
            $('.w_main_left').empty().append('<div class="zwwz">暂无文章</div>');
        }

        $.each(ar, function (k,v) {

            var zan = v.isLike == 1?'<i id="heart-'+v.articleId+'" data-value="'+v.isLike+'" class="glyphicon glyphicon-heart"></i>':
                '<i id="heart-'+v.articleId+'" data-value="'+v.isLike+'" class="glyphicon glyphicon-heart-empty"></i>';

            $('.contentList').append('<div class="panel panel-default w_article_item"><div class="panel-body"><div class="row">' +
                '<div class="col-xs-6 col-md-3"><a href="article_detail.html?id='+v.articleId+'" class="thumbnail w_thumbnail">' +
                '<img src="'+v.base64Img+'" alt="..."></a></div><h4 class="media-heading">' +
                '<a class="title" href="article_detail.html?id='+v.articleId+'">'+v.articleTitle+'</a></h4><p>'+getLabelHtml(v.labelName)+'</p>' +
                '<p class="w_list_overview overView" style="overflow:hidden; text-overflow:ellipsis; display:-webkit-box; -webkit-box-orient:vertical; -webkit-line-clamp:3;">' +
                ''+A.withoutHtml(v.articleContent)+'</p>' +
                '<p class="count_r"><span class="count"><i class="glyphicon glyphicon-user"></i><a href="#">'+v.author+'</a></span> ' +
                '<span class="count"><i class="glyphicon glyphicon-eye-open"></i>阅读:'+v.articleViews+'</span><span class="count">' +
                '<i class="glyphicon glyphicon-comment"></i>评论:'+v.articleCommentCount+'</span><span class="count">' +
                '<i class="glyphicon glyphicon-time"></i>'+v.articleDate+'</span><span class="count">' +
                '<a href="javascript:dianLike('+v.articleId+')">' + zan+
                '<span style="margin-left: 6px;" id="count-'+v.articleId+'">'+v.articleLikeCount+'</span></a></span></p></div></div></div>');
        })

        // site
        A.siteArticleHTML(data.data.siteArticle);
        A.friendShipLinkHTML(data.data.friendShipLinkVOS);
        return
    }

}

$(function(){
    A.initHeadPic();

    if (typeof id!='undefined' && id!=null && id!=""){
        url = url + "?id="+id;
    } else {
        url = url + "?id=0";
    }

    if (typeof search!='undefined' && search!=null && search!="") {
        url = url + "&search="+search;

        $('#search-content').val(search);

    }

    var username = $.cookie('LoginName');
    if (V.isNaN(username)) {
        url = url + "&username="+username;
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
            L.loadSort(data);
            $("#page").Page({
                totalPages: data.data.pagesize,//分页总数
                liNums: 3,//分页的数字按钮数(建议取奇数)
                activeClass: 'activP', //active 类样式定义
                callBack : function(page){
                    A.jsonpJqAjax(url+'&pid='+page, L.loadSort, '');
                }
            });
        },
        error: function (e) {
            throw new Error('jsonp ajax request occurs error.'+JSON.stringify(e));
        }
    });
})