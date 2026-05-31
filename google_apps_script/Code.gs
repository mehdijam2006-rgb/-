/**
 * Google Apps Script for Letter Management System Sync
 * Deploy as Web App: Execute as Me, Anyone can access
 */

const SPREADSHEET_ID = 'YOUR_SPREADSHEET_ID_HERE';
const LETTERS_SHEET   = 'Letters';
const FINANCIAL_SHEET = 'Financial';

function doPost(e) {
  try {
    const payload = JSON.parse(e.postData.contents);

    if (payload.action === 'sync') {
      syncLetters(payload.letters || []);
      syncReceipts(payload.receipts || []);
      return ContentService
        .createTextOutput(JSON.stringify({ success: true, message: 'Sync completed' }))
        .setMimeType(ContentService.MimeType.JSON);
    }

    return errorResponse('Unknown action');
  } catch (err) {
    return errorResponse(err.toString());
  }
}

function syncLetters(letters) {
  if (!letters.length) return;
  const ss    = SpreadsheetApp.openById(SPREADSHEET_ID);
  let   sheet = ss.getSheetByName(LETTERS_SHEET);

  if (!sheet) {
    sheet = ss.insertSheet(LETTERS_SHEET);
    sheet.appendRow([
      'ID', 'شماره اتوماتیک', 'شماره دستی', 'تاریخ نامه',
      'فرستنده', 'مهلت', 'متقاضی',
      'تاریخ پاسخ', 'شماره پاسخ اتوماتیک', 'شماره پاسخ دستی', 'وضعیت'
    ]);
  }

  const existingData = sheet.getDataRange().getValues();
  const existingIds  = new Set(existingData.slice(1).map(row => String(row[0])));

  letters.forEach(letter => {
    const row = [
      letter.id,
      letter.autoNumber,
      letter.manualLetterNumber || '',
      letter.letterDateShamsi,
      letter.sender,
      letter.deadlineShamsi,
      letter.applicantName,
      letter.responseDateShamsi || '',
      letter.autoResponseNumber || '',
      letter.manualResponseNumber || '',
      letter.status === 'archived' ? 'بایگانی' : 'فعال'
    ];

    if (existingIds.has(String(letter.id))) {
      // Update existing row
      const rowIndex = existingData.findIndex(r => String(r[0]) === String(letter.id));
      if (rowIndex > 0) {
        sheet.getRange(rowIndex + 1, 1, 1, row.length).setValues([row]);
      }
    } else {
      sheet.appendRow(row);
    }
  });
}

function syncReceipts(receipts) {
  if (!receipts.length) return;
  const ss    = SpreadsheetApp.openById(SPREADSHEET_ID);
  let   sheet = ss.getSheetByName(FINANCIAL_SHEET);

  if (!sheet) {
    sheet = ss.insertSheet(FINANCIAL_SHEET);
    sheet.appendRow([
      'ID', 'شناسه نامه', 'شماره فیش', 'تاریخ فیش',
      'مبلغ (ریال)', 'توضیحات', 'دریافت‌شده', 'تاریخ دریافت'
    ]);
  }

  const existingData = sheet.getDataRange().getValues();
  const existingIds  = new Set(existingData.slice(1).map(row => String(row[0])));

  receipts.forEach(receipt => {
    const row = [
      receipt.id,
      receipt.letterId,
      receipt.receiptNumber,
      receipt.receiptDateShamsi,
      receipt.amount,
      receipt.description || '',
      receipt.isReceived ? 'بله' : 'خیر',
      receipt.receivedDateShamsi || ''
    ];

    if (existingIds.has(String(receipt.id))) {
      const rowIndex = existingData.findIndex(r => String(r[0]) === String(receipt.id));
      if (rowIndex > 0) {
        sheet.getRange(rowIndex + 1, 1, 1, row.length).setValues([row]);
      }
    } else {
      sheet.appendRow(row);
    }
  });
}

function errorResponse(message) {
  return ContentService
    .createTextOutput(JSON.stringify({ success: false, message: message }))
    .setMimeType(ContentService.MimeType.JSON);
}
