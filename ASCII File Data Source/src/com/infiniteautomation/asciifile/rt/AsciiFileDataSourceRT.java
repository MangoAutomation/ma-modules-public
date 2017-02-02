package com.infiniteautomation.asciifile.rt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.infiniteautomation.mango.regex.MatchCallback;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;

/**
 * @author Phillip Dunlap
 */

public class AsciiFileDataSourceRT extends PollingDataSource<AsciiFileDataSourceVO> implements FileAlterationListener {
	private static final Log LOG = LogFactory.getLog(AsciiFileDataSourceRT.class);

	public static final int POINT_READ_EXCEPTION_EVENT = 1;
	public static final int POINT_WRITE_EXCEPTION_EVENT = 2;
	public static final int DATA_SOURCE_EXCEPTION_EVENT = 3;
	public static final int POINT_READ_PATTERN_MISMATCH_EVENT = 4;
	public static final int POLL_ABORTED_EVENT = 5;

	private File file; // File
	private FileAlterationObserver fobs;

	public AsciiFileDataSourceRT(AsciiFileDataSourceVO vo) {
		super(vo);
		setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
	}

	/**
	 * Load a file path
	 * 
	 * @throws Exception
	 */
	public boolean connect() throws Exception {
		AsciiFileDataSourceVO vo = (AsciiFileDataSourceVO) this.getVo();

		this.file = new File(vo.getFilePath());
		if (!file.exists()) {
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
					new TranslatableMessage("file.event.fileNotFound", vo.getFilePath()));
			return false;
		} else if (!file.canRead()) {
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
					new TranslatableMessage("file.event.readFailed", vo.getFilePath()));
			return false;
		} else {
			this.fobs = new FileAlterationObserver(this.file);
			this.fobs.initialize();
			this.fobs.addListener(this);

			return true;
		}

	}

	@Override
	public void initialize() {
		boolean connected = false;
		try {
			connected = this.connect();
		} catch (Exception e) {
			LOG.debug("Error while initializing data source", e);
			String msg = e.getMessage();
			if (msg == null) {
				msg = "Unknown";
			}
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
					new TranslatableMessage("file.event.readFailed", msg));

		}

		if (connected) {
			returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
		}
		super.initialize();

	}

	@Override
	public void terminate() {
		super.terminate();
		if (this.file != null) {
			try {
				this.fobs.destroy();
			} catch (Exception e) {
				LOG.debug("Error destroying file observer");
				raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
						new TranslatableMessage("file.event.obsDestroy", e.getMessage()));
			}
			this.file = null;
		}

	}

	@Override
	public void setPointValueImpl(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
		// TODO: Enable Regex replace
		// no-op
	}

	@Override
	public void onDirectoryChange(File dir) {
		fileEvent();
	}

	@Override
	public void onDirectoryCreate(File dir) {
		// no-op
	}

	@Override
	public void onDirectoryDelete(File dir) {
		// no-op
	}

	@Override
	public void onFileCreate(File f) {
		// no-op
	}

	@Override
	public void onFileDelete(File f) {
		// no-op
	}

	@Override
	public void onFileChange(File f) {
		fileEvent();
	}

	@Override
	public void onStart(FileAlterationObserver obs) {
		fileEvent();
	}

	@Override
	public void onStop(FileAlterationObserver obs) {
		// no-op
	}

	private void fileEvent() {
		// Should never happen
		if (this.file == null) {
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
					new TranslatableMessage("file.event.readFailedFileNotSetup"));
			return;
		}

		// The file is modified or we've just started, so read it.
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.file));

			String msg;
			if (!this.dataPoints.isEmpty()) {

				// TODO optimize to be better than numLines*numPoints
				while ((msg = reader.readLine()) != null) {
					// Give all points the chance to find their data
					for (final DataPointRT dp : this.dataPoints) {
						AsciiFilePointLocatorRT pl = dp.getPointLocator();
						final AsciiFilePointLocatorVO plVo = pl.getVo();

						MatchCallback callback = new MatchCallback() {

							@Override
							public void onMatch(String pointIdentifier, PointValueTime value) {
								if (!plVo.getHasTimestamp())
									dp.updatePointValue(value);
								else
									dp.savePointValueDirectToCache(value, null, true, true);
							}

							@Override
							public void pointPatternMismatch(String message, String pointValueRegex) {
								//N/A
							}

							@Override
							public void messagePatternMismatch(String message, String messageRegex) {
								//N/A
							}

							@Override
							public void pointNotIdentified(String message, String messageRegex,
									int pointIdentifierIndex) {
								raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), false,
										new TranslatableMessage("file.event.insufficientGroups",
												dp.getVO().getExtendedName()));
							}

							@Override
							public void matchGeneralFailure(Exception e) {
								if (e instanceof ParseException)
									raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
											new TranslatableMessage("file.event.dateParseFailed", e.getMessage()));
								else if (e instanceof NumberFormatException) {
									raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
											new TranslatableMessage("file.event.notNumber", e.getMessage()));
								} else
									raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
											new TranslatableMessage("file.event.readFailed", e.getMessage()));
							}

						};

						matchPointValueTime(msg, pl.getValuePattern(), plVo.getPointIdentifier(),
								plVo.getPointIdentifierIndex(), plVo.getDataTypeId(),
								plVo.getValueIndex(), plVo.getHasTimestamp(), plVo.getTimestampIndex(),
								plVo.getTimestampFormat(), callback);

					}
				}
				reader.close();
				returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
			}
		} catch (FileNotFoundException e) {
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
					new TranslatableMessage("file.event.fileNotFound", e.getMessage()));
		} catch (IOException e) {
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
					new TranslatableMessage("file.event.readFailed", e.getMessage()));
		} catch (NumberFormatException e) {
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
					new TranslatableMessage("file.event.notNumber", e.getMessage()));
		}

	}

	@Override
	protected void doPoll(long time) {
		if (fobs != null)
			fobs.checkAndNotify();

	}

	public static void matchPointValueTime(String message, Pattern pattern, String pointIdentifier,
			int pointIdentifierIndex, int dataTypeId, int valueIndex, boolean hasTimestamp, int timestampIndex,
			String timestampFormat, MatchCallback callback) {
		Matcher messageMatcher = pattern.matcher(message);
		if (messageMatcher.find()) {
			if (LOG.isDebugEnabled())
				LOG.debug("Message matched regex: " + pattern.pattern());

			// Parse out the Identifier
			//String matchedPointIdentifier = null;
			try {
				if (pointIdentifierIndex > messageMatcher.groupCount() || valueIndex > messageMatcher.groupCount()) {
					callback.pointNotIdentified(message, pattern.pattern(), pointIdentifierIndex);
				} else if (pointIdentifier.equals(messageMatcher.group(pointIdentifierIndex))) {
					if (LOG.isDebugEnabled())
						LOG.debug("Point Identified: " + messageMatcher.group(pointIdentifierIndex));

					String value = messageMatcher.group(valueIndex);
					PointValueTime newValue;
					Date dt;
					if (hasTimestamp && !timestampFormat.equals(".")) {
						SimpleDateFormat fmt = new SimpleDateFormat(timestampFormat);
						dt = fmt.parse(messageMatcher.group(timestampIndex));
					} else if (hasTimestamp) {
						dt = new Date(Long.parseLong(messageMatcher.group(timestampIndex)));
					} else {
						dt = new Date();
					}

					// Switch on the type
					switch (dataTypeId) {
					case DataTypes.ALPHANUMERIC:
						newValue = new PointValueTime(value, dt.getTime());
						break;
					case DataTypes.NUMERIC:
						newValue = new PointValueTime(Double.parseDouble(value), dt.getTime());
						break;
					case DataTypes.MULTISTATE:
						newValue = new PointValueTime(Integer.parseInt(value), dt.getTime());
						break;
					case DataTypes.BINARY:
						newValue = new PointValueTime(Boolean.parseBoolean(value), dt.getTime());
						break;
					default:
						throw new ShouldNeverHappenException("Uknown Data type for point");
					}
					callback.onMatch(messageMatcher.group(pointIdentifierIndex), newValue);
				}
			} catch (Exception e) {
				callback.matchGeneralFailure(e);
				return;
			}
		}
	}
}
