(function($){
	function crateCommentInfo(obj){

		if(typeof(obj.time) == "undefined" || obj.time == ""){
			obj.time = getNowDateFormat();
		}
        // glyphicon glyphicon-thumbs-up
		var el = "<div class='comment-info' data-id='"+obj.id+"' data-uid='"+obj.uid+"'><header><img src='"+obj.img+"'></header><div class='comment-right'>" +
			"<h3 style='font-size: 15px;font-weight: bold;'>"+obj.replyName+"</h3>"
				+"<div class='comment-content-header'><span><i class='glyphicon glyphicon-time'></i>&nbsp;"+obj.time+"</span>";

		var likehtml = "<i class='glyphicon glyphicon-thumbs-up' data-value='"+obj.islike+"'></i><span class='likenum'>"+obj.likenum;

		if (obj.islike == '1'){
			likehtml = "<i class='glyphicon glyphicon-thumbs-up haslike' data-value='"+obj.islike+"'></i><span class='likenum'>"+obj.likenum;
		}

		el = el+"</div><p class='content-p'><span class='cp-l'>"+obj.content+"</span><span class='cp-r'>"+likehtml+"</span></span>" +
			"</p><div class='comment-content-footer'><div class='row'><div class='col-md-10'>";

		var delHtml = "<span class='reply-btn' style='color: #84cc36;'>回复</span>";
		if (V.isNaN($.cookie('LoginName')) && $.cookie('userId')==obj.uid) {
            delHtml = "<span class='comment-del'>删除</span>";
		}
		
		el = el + "</div><div class='col-md-2'>"+delHtml+"</div></div></div><div class='reply-list'>";
		if(obj.replyBody != "" && obj.replyBody.length > 0){
			var arr = obj.replyBody;
			for(var j=0;j<arr.length;j++){
				var replyObj = arr[j];
				el = el+createReplyComment(replyObj);
			}
		}
		el = el+"</div></div></div>";
		return el;
	}
	
	//返回每个回复体内容
	function createReplyComment(reply){
        var delHtml = "<span class='reply-list-btn' style='color: #84cc36;' data-id='"+reply.id+"' id='c"+reply.id+"'>回复</span>";
        if (V.isNaN($.cookie('LoginName')) && $.cookie('userId')==reply.uid) {
            delHtml = "<span class='comment-del-reply' data-id='"+reply.id+"'>删除</span>";
        }
		var replyEl = "<div class='reply'><div><a href='javascript:void(0)' class='replyname'  data-id='"+reply.uid+"'>"+reply.replyName+"</a>:<a href='javascript:void(0)'>@"+reply.beReplyName+"</a><span>"+reply.content+"</span></div>"
						+ "<p><span>"+reply.time+"</span>  "+delHtml+"</p></div>";
		return replyEl;
	}
	function getNowDateFormat(){
		var nowDate = new Date();
		var year = nowDate.getFullYear();
		var month = filterNum(nowDate.getMonth()+1);
		var day = filterNum(nowDate.getDate());
		var hours = filterNum(nowDate.getHours());
		var min = filterNum(nowDate.getMinutes());
		var seconds = filterNum(nowDate.getSeconds());
		return year+"-"+month+"-"+day+" "+hours+":"+min+":"+seconds;
	}
	function filterNum(num){
		if(num < 10){
			return "0"+num;
		}else{
			return num;
		}
	}
	function replyClick(el){
		el.parent().parent().append("<div class='replybox'><textarea cols='80' rows='50' placeholder='回复评论' class='mytextarea' ></textarea>" +
			"<span style='padding-left: 92%;color: #8a7c7f;font-weight: bold;' class='send'>发送</span></div>")
		.find(".send").click(function(){
			var content = $(this).prev().val();
			if(content != ""){
				var parentEl = $(this).parent().parent().parent().parent();
				var obj = new Object();
				obj.replyName=$.cookie('nickname');
				if(el.parent().parent().hasClass("reply")){
					obj.beReplyName = el.parent().parent().find("a:first").text();
				}else{
					obj.beReplyName=parentEl.find("h3").text();
				}
				obj.content=content;
				obj.time = getNowDateFormat();
                obj.uid=$.cookie('userId');
				var replyString = createReplyComment(obj);

                var cid = $(this).closest(".comment-info").data('id');

                var fuid = el.parent().parent().find("a:first").data('id');
                if (!V.isNaN(fuid)) {
                    fuid = $(this).closest(".comment-info").data('uid');
				}
				//增加子评论
                addComment(UrlParm.parm("id"), obj.uid, fuid, cid , content, function() {

                })

				$(".replybox").remove();
				// parentEl.find(".reply-list").append(replyString).find(".reply-list-btn:last").click(function(){layer.msg("不能回复自己");});
				parentEl.find(".reply-list").append(replyString);
			}else{
				layer.msg('回复的内容不能为空哦');
			}
		});
	}

    // 删除主评论
    $(document).on('click', '.comment-del', function () {
        var $comment = $(this);
        layer.confirm('<b style="font-size: 14px;font-family: \'Microsoft Yahei\'">确定删除此评论？</b>', {
            btn: ['确定','取消'],
        }, function (index) {
            $comment.closest(".comment-info").remove();
            layer.close(index);
            //clean
            var id = $comment.closest(".comment-info").data('id');
            var param = "commentId="+id+"&articleId="+UrlParm.parm("id");
            jsonAjaxJsonp("GET", u+"/article/comment/del", A.paramToken(param), function (data) {
                if (data.errorCode != '0') {
                    layer.msg(data.message);
                }
                if ($('.comment-info').length==0) {
                    $('.zwpl').show();
                    $('#tagnew').hide();
                }
                return
            })
        }, function(index){
            layer.close(index);
        });
    });

    // 删除子评论
    $(document).on('click', '.comment-del-reply', function () {
        var $comment = $(this);
        var parentCommentId = $comment.closest(".comment-info").data('id');
        layer.confirm('<b style="font-size: 14px;font-family: \'Microsoft Yahei\'">确定删除此评论？</b>', {
            btn: ['确定','取消'],
        }, function (index) {
            $comment.closest(".reply").remove();
            layer.close(index);
            var param = "commentId="+$comment.data('id')+"&parentCommentId="+parentCommentId+"&articleId="+UrlParm.parm("id");
            jsonAjaxJsonp("GET", u+"/article/comment/del", A.paramToken(param), function (data) {
                if (data.errorCode != '0') {
                    layer.msg(data.message);
                }
                return
            })
        }, function(index){
            layer.close(index);
        });
    });

    // 评论点赞
    $(document).on('click', '.cp-r', function () {

        if(!V.isNaN($.cookie('LoginName'))){
            A.goToLogin();
            return
        }

		var $thumbs =  $(this).find('.glyphicon-thumbs-up');

        var commentId = $(this).closest(".comment-info").data('id');

        var status = $thumbs.attr('data-value');

        var count = parseInt($(this).find('.likenum').html());

		if (status == '0') {
            $thumbs.attr('data-value', '1');
            $thumbs.addClass('haslike');
            $(this).find('.likenum').html(count+1)
		} else {
            $thumbs.attr('data-value', '0');
            $thumbs.removeClass('haslike');
            $(this).find('.likenum').html(count-1<0?0:count-1)
		}

		status = $thumbs.attr('data-value');

        $.ajax({
            url: u+"/comment/like?cid="+commentId+'&status='+status+'&username='+$.cookie('LoginName')+'&token='+$.cookie('token'),
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
    });

	$.fn.addCommentList=function(options){
		var defaults = {
			data:[],
			add:""
		}
		var option = $.extend(defaults, options);
		//加载数据
		if(option.data.length > 0){
            $('#tagnew').show();
            $('.zwpl').hide();
			var dataList = option.data;
			var totalString = "";
			for(var i=0;i<dataList.length;i++){
				var obj = dataList[i];
				var objString = crateCommentInfo(obj);
				totalString = totalString+objString;
			}
			$(this).append(totalString).find(".reply-btn").click(function(){
                if(!V.isNaN($.cookie('LoginName'))){
                    A.goToLogin('请先登录在进行评论哦');
                    return
                }
				if($(this).parent().parent().find(".replybox").length > 0){
					$(".replybox").remove();
				}else{
					$(".replybox").remove();
					replyClick($(this));
				}
			});
			$(".reply-list-btn").click(function(){
                if(!V.isNaN($.cookie('LoginName'))){
                    A.goToLogin('请先登录在进行评论哦');
                    return
                }
				if($(this).parent().parent().find(".replybox").length > 0){
					$(".replybox").remove();
				}else{
					$(".replybox").remove();
					replyClick($(this));
				}
			})
		}
		
		//添加新数据
		if(option.add != ""){
			obj = option.add;
			var str = crateCommentInfo(obj);
			$(this).prepend(str).find(".reply-btn").click(function(){
				replyClick($(this));
			});
		}
	}
	
	
})(jQuery);