var D = {
    load: function (data) {
        $('.contentList .box .event_year').empty();
        $('.contentList .box .event_list').empty();
        $.each(data.filing, function (k,v) {
            if (k==0) {
                $('.contentList .box .event_year').append('<li class="current"><label for="'+v.articleYear+'">'+v.articleYear+'</label></li>');
            } else {
                $('.contentList .box .event_year').append('<li><label for="'+v.articleYear+'">'+v.articleYear+'</label></li>');
            }
            var monHtml='';
            $.each(v.pfList, function (k, vv) {
                monHtml += "<li><span>"+vv.mon+"</span><p><span><a href='article_detail.html?id="+vv.articleId+"' target='_blank'>"+vv.articleTitle+"</a></span></p></li>";
            })
            $('.contentList .box .event_list').append('<div><h3 id="'+v.articleYear+'">'+v.articleYear+'</h3>'+monHtml+'</div>');
        });
    }
}

$(function(){
    A.initHeadPic();
    A.switchBarClass(4)
    var url = u+"/filing";
    A.jsonpJqAjax(url, D.load, function(){
        $('.contentList').find('label').click(function(){
            $('.event_year>li').removeClass('current');
            $(this).parent('li').addClass('current');
            var year = $(this).attr('for');
            $('#'+year).parent().prevAll('div').slideUp(800);
            $('#'+year).parent().slideDown(800).nextAll('div').slideDown(800);
        });
    });
});