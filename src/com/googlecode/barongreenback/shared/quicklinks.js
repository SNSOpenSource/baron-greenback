jQuery(document).ready(function () {

    var originalLabelColor = $('div.legend').css('background-color');
    var originalBorderColor = $('div.fieldset').css('border-top-color');

    $('div.legend:visible').each(function () {
        var legend = $(this).text();
        var quickLink = $('<a></a>').attr('href', '#').addClass('quicklink').text(legend);
        var listElement = $('<li></li>').append(quickLink);
        $('#quicklinks ul').append(listElement);
    });

    $('#quicklinks a').click(function (e) {
        e.preventDefault();

        var selectedLink = $(this).text();
        var selectedSectionLabel = $("div.legend").filter(function () {
            return $(this).text().toLowerCase() === selectedLink.toLowerCase();
        }).first();

        var sectionTop = selectedSectionLabel.offset().top;
        var navbarHeight = $('.navbar').height();

        $('html,body').animate({
            scrollTop: sectionTop - navbarHeight
        }, {
            duration: 500,
            queue: false
        });

        var highlightingColor = '#fff0c0';
        selectedSectionLabel.css('background-color', highlightingColor);
        selectedSectionLabel.animate({
            backgroundColor: originalLabelColor
        }, {
            duration: 2000,
            queue: false
        });

        var selectedSection = selectedSectionLabel.parent('div.fieldset');
        selectedSection.css({
            'background-color': highlightingColor,
            'border-color': highlightingColor
        });
        selectedSection.animate({
            backgroundColor: 'white',
            borderColor: originalBorderColor
        }, {
            duration: 2000,
            queue: false
        });

    });
});

