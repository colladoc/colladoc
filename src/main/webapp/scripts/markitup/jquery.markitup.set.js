markItUpSettings = {
	onShiftEnter: { keepDefault:false, replaceWith:'\n\n' },
	markupSet: [
        { name:'Heading', className:"headingButton", dropMenu: [
            { name:'Heading 1', openWith:'=', closeWith:'=' },
            { name:'Heading 2', openWith:'==', closeWith:'==' },
            { name:'Heading 3', openWith:'===', closeWith:'===' },
            { name:'Heading 4', openWith:'====', closeWith:'====' },
            { name:'Heading 5', openWith:'=====', closeWith:'=====' }
        ]},
        { separator:'---------------' },
		{ name:'Bold', key:'B', openWith:"'''", closeWith:"'''", className:"boldButton" },
		{ name:'Italic', key:'I', openWith:"''", closeWith:"''", className:"italicButton" },
		{ name:'Underline', key:'U', openWith:"__", closeWith:"__", className:"underlineButton" },
		{ separator:'---------------' },
		{ name:'Unordered list', openWith:'(!(* |!|*)!)', className:"ulButton" },
		{ name:'Ordered list', openWith:'(!(# |!|#)!)', className:"olButton" },
		{ separator:'---------------' },
        { name:'Uppercase', openWith:"^", closeWith:"^", className:"ucButton" },
		{ name:'Lowercase', openWith:",,", closeWith:",,", className:"lcButton" },
		{ separator:'---------------' },
		{ name:'Link', key:"L", openWith:"[[[![Link]!]", closeWith:"]]", className:"linkButton" },
		{ name:'Url', openWith:"[[[![Url:!:http://]!] ", closeWith:"]]", placeHolder:'Link name', className:"urlButton" },
		{ separator:'---------------' },
		{ name:'Code', openWith:"{{{", closeWith:"}}}", className:"codeButton" },
        { separator:'---------------' },
        { name:'Preview', className:'previewButton', call:'preview' }
	]
}
