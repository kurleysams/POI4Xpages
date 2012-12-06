/*
 * � Copyright WebGate Consulting AG, 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package biz.webgate.dominoext.poi.component.containers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletResponse;

import biz.webgate.dominoext.poi.component.data.ITemplateSource;
import biz.webgate.dominoext.poi.component.data.document.IDocumentBookmark;
import biz.webgate.dominoext.poi.component.kernel.DocumentProcessor;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.FacesAjaxComponent;
import com.ibm.xsp.util.FacesUtil;
import com.ibm.xsp.util.StateHolderUtil;
import com.ibm.xsp.webapp.XspHttpServletResponse;

public class UIDocument extends UIComponentBase implements FacesAjaxComponent {

	public static final String COMPONENT_TYPE = "biz.webgate.dominoext.poi.Document"; //$NON-NLS-1$
	public static final String COMPONENT_FAMILY = "biz.webgate.dominoext.poi.Document"; //$NON-NLS-1$
	public static final String RENDERER_TYPE = "biz.webgate.dominoext.poi.Document"; //$NON-NLS-1$

	private List<IDocumentBookmark> m_Bookmarks;
	private String m_pathInfo;
	private String m_DownloadFileName;
	private ITemplateSource m_TemplateSource;

	public UIDocument() {

		super();
		System.out.println("Creating UIDocument");
		setRendererType(RENDERER_TYPE);
	}

	public List<IDocumentBookmark> getBookmarks() {
		return m_Bookmarks;
	}

	public void setBookmarks(List<IDocumentBookmark> bookmarks) {
		m_Bookmarks = bookmarks;
	}

	public void addBookmark(IDocumentBookmark myBookmark) {
		if (m_Bookmarks == null) {
			m_Bookmarks = new ArrayList<IDocumentBookmark>();
		}
		m_Bookmarks.add(myBookmark);
	}

	public String getPathInfo() {
		if (null != m_pathInfo) {
			return m_pathInfo;
		}
		ValueBinding _vb = getValueBinding("pathInfo"); //$NON-NLS-1$
		if (_vb != null) {
			return (java.lang.String) _vb.getValue(FacesContext
					.getCurrentInstance());
		} else {
			return null;
		}
	}

	public void setPathInfo(String linkName) {
		m_pathInfo = linkName;
	}

	public String getDownloadFileName() {
		if (null != m_DownloadFileName) {
			return m_DownloadFileName;
		}
		ValueBinding _vb = getValueBinding("downloadFileName"); //$NON-NLS-1$
		if (_vb != null) {
			return (java.lang.String) _vb.getValue(FacesContext
					.getCurrentInstance());
		} else {
			return null;
		}
	}

	public void setDownloadFileName(String downloadFileName) {
		m_DownloadFileName = downloadFileName;
	}

	public ITemplateSource getTemplateSource() {
		return m_TemplateSource;
	}

	public void setTemplateSource(ITemplateSource templateSource) {
		m_TemplateSource = templateSource;
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	public boolean handles(FacesContext context) {
		String reqPathInfo = context.getExternalContext().getRequestPathInfo();
		if (StringUtil.isNotEmpty(reqPathInfo)) {
			String pathInfo = getPathInfo();
			if (StringUtil.isEmpty(pathInfo)) {
				return false;
			}
			if (!pathInfo.startsWith("/")) {
				pathInfo = "/" + pathInfo;
			}
			if (!pathInfo.endsWith("/")) {
				pathInfo += "/";
			}
			if (!reqPathInfo.startsWith("/")) {
				reqPathInfo = "/" + reqPathInfo;
			}
			if (!reqPathInfo.endsWith("/")) {
				reqPathInfo += "/";
			}
			if (reqPathInfo.startsWith(pathInfo)) {
				return true;
			}
		}
		return false;
	}

	public void processAjaxRequest(FacesContext context) throws IOException {
		HttpServletResponse httpResponse = (HttpServletResponse) context
				.getExternalContext().getResponse();

		// Disable the XPages response buffer as this will collide with the
		// engine one
		// We mark it as committed and use its delegate instead
		if (httpResponse instanceof XspHttpServletResponse) {
			XspHttpServletResponse r = (XspHttpServletResponse) httpResponse;
			r.setCommitted(true);
			httpResponse = r.getDelegate();
		}

		ITemplateSource itsCurrent = getTemplateSource();
		if (itsCurrent == null) {
			// TODO: BUILD Error
			return;
		}

		System.out.println("processing....");
		try {
			DocumentProcessor.getInstance().generateNewFile(itsCurrent,
					getBookmarks(), httpResponse, getDownloadFileName());
		} catch (Exception e) {
			try {
				e.printStackTrace();
				e.printStackTrace(httpResponse.getWriter());
			} catch (Exception e2) {
				e2.printStackTrace();
				e.printStackTrace();
			}
		}
	}

	// SAVING AND RESTORING
	@Override
	public Object saveState(FacesContext context) {
		try {
			Object[] state = new Object[5];
			state[0] = super.saveState(context);
			state[1] = m_DownloadFileName;
			state[2] = m_pathInfo;
			state[3] = FacesUtil
					.objectToSerializable(context, m_TemplateSource);
			state[4] = StateHolderUtil.saveList(context, m_Bookmarks);
			return state;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void restoreState(FacesContext context, Object value) {
		Object[] state = (Object[]) value;
		super.restoreState(context, state[0]);
		m_DownloadFileName = (String) state[1];
		m_pathInfo = (String) state[2];
		m_TemplateSource = (ITemplateSource) FacesUtil.objectFromSerializable(
				context, this, state[3]);
		m_Bookmarks = StateHolderUtil.restoreList(context, this, state[4]);

	}
}
