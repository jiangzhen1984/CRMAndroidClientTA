package com.auguraclient.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.auguraclient.db.ContentDescriptor;

public class AuguaModuleImpl implements IAuguraModule {

	private Context c;

	public AuguaModuleImpl(Context c) {
		if (c == null) {
			throw new NullPointerException(" context can't be null");
		}
		this.c = c;
	}
	
	
	
	
	public Uri saveUpdateReocrd(UpdateRecord ur) throws Exception {
		if (c == null) {
			throw new NullPointerException(" context is null");
		}
		ContentResolver cr = c.getContentResolver();
		if (cr == null || ur == null) {
			throw new NullPointerException(
					" ContentResolver and project can't be null");
		}
		
		ContentValues cv = new ContentValues();

		cv.put(ContentDescriptor.UpdateDesc.Cols.TYPE,
				ContentDescriptor.UpdateDesc.TYPE_ENUM_CHECKPOINT);
		cv.put(ContentDescriptor.UpdateDesc.Cols.PRO_ID,ur.getProId());
		cv.put(ContentDescriptor.UpdateDesc.Cols.PRO_ORDER_ID,ur.getProOrderId());
		cv.put(ContentDescriptor.UpdateDesc.Cols.RELATE_ID,ur.getRelatedId());
		cv.put(ContentDescriptor.UpdateDesc.Cols.FLAG, ur.getFlag());
		
		Uri uri = cr.insert(ContentDescriptor.UpdateDesc.CONTENT_URI, cv);
		long id = ContentUris.parseId(uri);
		ur.setnID((int) id);
		return uri;
	}
	

	@Override
	public Uri saveProject(Project p) throws Exception {
		if (c == null) {
			throw new NullPointerException(" context is null");
		}
		ContentResolver cr = c.getContentResolver();
		if (cr == null || p == null) {
			throw new NullPointerException(
					" ContentResolver and project can't be null");
		}
		ContentValues cv = new ContentValues();
		cv.put(ContentDescriptor.ProjectDesc.Cols.PRO_ID, p.getId());
		cv.put(ContentDescriptor.ProjectDesc.Cols.NAME, p.getName());
		cv.put(ContentDescriptor.ProjectDesc.Cols.TEXT, p.getText());
		cv.put(ContentDescriptor.ProjectDesc.Cols.SYNC_FLAG, "1");
		Uri uri = cr.insert(ContentDescriptor.ProjectDesc.CONTENT_URI, cv);
		long id = ContentUris.parseId(uri);
		p.setnID((int) id);
		return uri;
	}

	@Override
	public Uri saveProjectOrder(ProjectOrder po) throws Exception {
		if (c == null) {
			throw new NullPointerException(" context is null");
		}
		ContentResolver cr = c.getContentResolver();
		if (cr == null || po == null || po.getProject() == null) {
			throw new NullPointerException(
					" ContentResolver and ProjectOrder and order belongs to project can't be null");
		}
		ContentValues cv = getContentValues(po);
		Uri uri = cr.insert(ContentDescriptor.ProjectOrderDesc.CONTENT_URI, cv);
		long id = ContentUris.parseId(uri);
		po.setnID((int) id);
		return uri;
	}

	@Override
	public Uri saveCheckpoint(ProjectCheckpoint pcp) throws Exception {
		if (c == null) {
			throw new NullPointerException(" context is null");
		}
		ContentResolver cr = c.getContentResolver();
		if (cr == null || pcp == null || pcp.getProjectItem() == null
				|| pcp.getProjectItem().getProject() == null) {
			throw new NullPointerException(
					" ContentResolver and ProjectOrder and order belongs to project can't be null");
		}

		ContentValues cv = getContentValues(pcp);
		Uri uri = cr.insert(
				ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI, cv);
		long id = ContentUris.parseId(uri);
		pcp.setnID((int) id);
		return uri;
	}

	@Override
	public int updateProject(Project p) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateProjectOrder(ProjectOrder p) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateCheckpoint(ProjectCheckpoint pcp) throws Exception {
		if (c == null) {
			throw new NullPointerException(" context is null");
		}
		ContentResolver cr = c.getContentResolver();
		if (cr == null || pcp == null || pcp.getProjectItem() == null
				|| pcp.getProjectItem().getProject() == null) {
			throw new NullPointerException(
					" ContentResolver and ProjectOrder and order belongs to project can't be null");
		}

		ContentValues cv = getContentValues(pcp);
		int ret = cr.update(
				ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI, cv,
				ContentDescriptor.ProjectCheckpointDesc.Cols.ID + "=?",
				new String[] { pcp.getnID() + "" });
		return ret;
	}

	@Override
	public void deleteProject(Project p) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteProjectOrder(ProjectOrder p) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteCheckpoint(ProjectCheckpoint p) throws Exception {
		// TODO Auto-generated method stub

	}

	public int deleteFromDB(Class<? extends AbstractModel> cls,
			String whereCondition, String[] args) {
		if (c == null) {
			throw new NullPointerException(" context is null");
		}

		if (cls == null) {
			throw new NullPointerException(" cls can't be null");
		}
		Uri uri = null;
		if (cls.getName().equals(Project.class.getName())) {
			uri = ContentDescriptor.ProjectDesc.CONTENT_URI;
		} else if (cls.getName().equals(ProjectOrder.class.getName())) {
			uri = ContentDescriptor.ProjectOrderDesc.CONTENT_URI;
		} else if (cls.getName().equals(ProjectCheckpoint.class.getName())) {
			uri = ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI;
		} else if (cls.getName().equals(Photo.class.getName())) {

		} else if (cls.getName().equals(UpdateRecord.class.getName())) {
			uri = ContentDescriptor.UpdateDesc.CONTENT_URI;
		}else {
			throw new RuntimeException(" unknow class type");
		}

		return c.getContentResolver().delete(uri, whereCondition, args);
	}
	
	

	public int update(AbstractModel model, String whereCondition, String[] args) {
		if (c == null) {
			throw new NullPointerException(" context is null");
		}

		if (model == null) {
			throw new NullPointerException(" cls can't be null");
		}

		ContentValues cv = null;
		Uri uri = null;

		if (model instanceof Project) {
			uri = ContentDescriptor.ProjectDesc.CONTENT_URI;

		} else if (model instanceof ProjectOrder) {
			uri = ContentDescriptor.ProjectOrderDesc.CONTENT_URI;
			cv = getContentValues((ProjectOrder) model);

		} else if (model instanceof ProjectCheckpoint) {
			uri = ContentDescriptor.ProjectCheckpointDesc.CONTENT_URI;
			cv = getContentValues((ProjectCheckpoint) model);

		} else if (model instanceof Photo) {

		} else {
			throw new RuntimeException(" unknow class type");
		}
		
		return c.getContentResolver().update(uri, cv, whereCondition, args);

	}

	private ContentValues getContentValues(ProjectCheckpoint pcp) {
		ContentValues cv = new ContentValues();

		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NAME, pcp.getName());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.DESCRIPTION,
				pcp.getDescription());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECKPOINT_ID,
				pcp.getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_COMMENT,
				pcp.getQcComments());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_STATUS,
				pcp.getQcStatus());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ID, pcp
				.getProjectItem().getProject().getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PRO_ORDER_ID, pcp
				.getProjectItem().getId());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CATEGORY,
				pcp.getCategory());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.CHECK_TYPE,
				pcp.getCheckType());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.QC_ACTION,
				pcp.getQcAction());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.NUMBER_DEFECT,
				pcp.getNumberDefect());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_NAME,
				pcp.getPhotoName());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_SMALL_PATH,
				pcp.getPhotoPath());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_BIG_PATH,
				pcp.getPhotoPath());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.PHOTO_LOCAL_PATH, pcp.getUploadPhotoAbsPath());
		cv.put(ContentDescriptor.ProjectCheckpointDesc.Cols.FLAG,
				pcp.getFlag() == null ? "0" : pcp.getFlag());
		return cv;
	}

	private ContentValues getContentValues(ProjectOrder po) {
		ContentValues cv = new ContentValues();

		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.NAME, po.getName());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.DESCRIPTION,
				po.getDescription());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.ORD_ID, po.getId());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.PRO_ID, po.getProject()
				.getId());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QC_COMMENT,
				po.getQcComment());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QC_STATUS,
				po.getQcStatus());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY,
				po.getQuantity());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.QUANTITY_CHECKED,
				po.getQuantityChecked());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_NAME,
				po.getPhotoName());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_SMALL_PATH,
				po.getPhotoPath());
		cv.put(ContentDescriptor.ProjectOrderDesc.Cols.PHOTO_LOCAL_BIG_PATH,
				po.getPhotoBigPath());
		return cv;
	}
	
	
	

}
