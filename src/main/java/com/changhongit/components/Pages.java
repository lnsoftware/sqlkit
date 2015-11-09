package com.changhongit.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;


@Import(stylesheet = "pages.css", library = "pages.js")
public class Pages {
	@Parameter(required = true)
	private int totalRows;
	@Parameter(required = true)
	private int rowsPerPage;
	@Parameter(required = true)
	private int currentPage;
	@Parameter(value = "3")
	private int range;
	@Parameter
	private String zone;
	private int maxPages;
	@Inject
	private ComponentResources resources;
	@Environmental
	private ClientBehaviorSupport clientBehaviorSupport;
	@Environmental
	private JavaScriptSupport jsSupport;

	void beginRender(MarkupWriter writer) {
		currentPage = currentPage == 0 ? 1 : currentPage;
		int availableRows = totalRows;
		maxPages = ((availableRows - 1) / rowsPerPage) + 1;
		if (maxPages < 2) {
			writer.writeRaw("共" + totalRows + "条");
			return;
		}
		writer.element("div", "class", "pages_div");
		writerBackLink(writer, currentPage - 1);
		int low = currentPage - range;
		int high = currentPage + range;
		low = low < 1 ? 1 : low;
		high = high > maxPages ? maxPages : high;
		for (int i = low; i <= high; i++)
			writePageLink(writer, i);
		writerNextLink(writer, currentPage + 1);
        writerPointLink(writer);
		writer.end();
	}

	private void writePageLink(MarkupWriter writer, int pageIndex) {
		if (pageIndex < 1 || pageIndex > maxPages)
			return;
		if (pageIndex == currentPage) {
			writer.element("span", "class", "pages_span_button");
            writer.element("a", "href", "#", "class", "pages_span_previous_button");
			writer.writeRaw(pageIndex + "");
			writer.end();
			writer.end();
			writer.writeRaw("&nbsp;");
			return;
		}
		Object[] context = zone == null ? new Object[] { pageIndex }
				: new Object[] { pageIndex, zone };
		Link link = resources.createEventLink(EventConstants.ACTION, context);
        writer.element("span","class","pages_span_button");
		Element element = writer.element("a", "href",
				zone == null ? link : "#", "class", "pages_span_a_button", "title", "跳到第"
						+ pageIndex + "页");
		writer.write(Integer.toString(pageIndex));
		writer.end();
        writer.end();
		writer.writeRaw("&nbsp;");
		if (zone != null) {
			String id = jsSupport.allocateClientId(resources);
			element.attribute("id", id);
			clientBehaviorSupport.linkZone(id, zone, link);
		}
	}

	private void writerBackLink(MarkupWriter writer, int pageIndex) {
		if (pageIndex < 1) {
//            writer.element("span", "class", "pages_span_button");
//            writer.element("a", "href", "#", "class", "previous_page");
//			writer.writeRaw("<上一页");
//            writer.end();
//            writer.end();
//			writer.writeRaw("&nbsp;");
			return;
		}
		pageIndex = pageIndex < 1 ? 1 : pageIndex;
		Object[] context = zone == null ? new Object[] { pageIndex }
				: new Object[] { pageIndex, zone };
		Link link = resources.createEventLink(EventConstants.ACTION, context);
        writer.element("span","class","pages_span_button");
		Element element = writer.element("a", "href",
				zone == null ? link : "#", "class",
				pageIndex == currentPage ? "pagebtnnone" : "pages_span_a_button", "title",
				"跳到上一页");
		writer.write("<上一页");
		writer.end();
		writer.end();
		writer.writeRaw("&nbsp;");
		if (zone != null) {
			String id = jsSupport.allocateClientId(resources);
			element.attribute("id", id);
			clientBehaviorSupport.linkZone(id, zone, link);
		}
	}

	private void writerNextLink(MarkupWriter writer, int pageIndex) {
		if (pageIndex > maxPages) {
//			writer.element("span", "class", "previous_page");
//			writer.writeRaw("下一页>");
//			writer.end();
//			writer.writeRaw("&nbsp;");
			return;
		}
		pageIndex = pageIndex > maxPages ? maxPages : pageIndex;
		Object[] context = zone == null ? new Object[] { pageIndex }
				: new Object[] { pageIndex, zone };
		Link link = resources.createEventLink(EventConstants.ACTION, context);
        writer.element("span","class","pages_span_button");
		Element element = writer
				.element("a", "href", zone == null ? link : "#", "class",
						"pages_span_a_button", "title", "跳到下一页");
		writer.write("下一页>");
		writer.end();
        writer.end();
		writer.writeRaw("&nbsp;");
		if (zone != null) {
			String id = jsSupport.allocateClientId(resources);
			element.attribute("id", id);
			clientBehaviorSupport.linkZone(id, zone, link);
		}
	}

	private void writerPointLink(MarkupWriter writer) {
		Link link = resources.createEventLink(EventConstants.ACTION);
        writer.element("span","class","pages_span_button");
		writer.writeRaw("&nbsp;&nbsp;共" + maxPages + "页&nbsp;到第&nbsp;");
		writer.element("input", "type", "text", "class", "pages_span_input", "id",
				"Pages-btn-pageinput");
		writer.end();
		writer.writeRaw("&nbsp;页&nbsp;");

		writer.element("a","href", "#", "class", "pages_span_a_button", "onclick", "pageinputcheck('"
				+ link.getBasePath() + "','" + link.copyWithBasePath("") + "',"
				+ currentPage + "," + maxPages + ")");
		writer.write("确定");
		writer.end();
		writer.end();
	}

	void onAction(int newPage) {
		currentPage = newPage;
	}

	boolean onAction(int newPage, String zone) {
		onAction(newPage);
		resources.triggerEvent(InternalConstants.GRID_INPLACE_UPDATE,
				new Object[] { zone }, null);
		return true; // abort event
	}
}
