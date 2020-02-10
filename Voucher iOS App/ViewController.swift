//
//  ViewController.swift
//  Voucher iOS App
//
//  Created by Rizwan Sattar on 11/7/15.
//  Copyright © 2015 Rizwan Sattar. All rights reserved.
//

import UIKit
import Voucher

class ViewController: UIViewController, VoucherServerDelegate {

    var server: VoucherServer?

    @IBOutlet var serverStatusLabel: UILabel!
    @IBOutlet var connectionStatusLabel: UILabel!

    deinit {
        self.server?.stop()
        self.server?.delegate = nil
        self.server = nil
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        let uniqueId = "VoucherTest"
        self.server = VoucherServer(uniqueSharedId: uniqueId)
        self.server?.delegate = self
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        self.server?.startAdvertising { (displayName, responseHandler) -> Void in

            let alertController = UIAlertController(title: "Allow Auth?", message: "Allow \"\(displayName)\" access to your login?", preferredStyle: .alert)
            alertController.addAction(UIAlertAction(title: "Not Now", style: .cancel, handler: { action in
                responseHandler(nil, nil)
            }))

            alertController.addAction(UIAlertAction(title: "Allow", style: .default, handler: { action in
                // For our authData, use a token string (to simulate an OAuth token, for example)
                let authData = "THIS IS AN AUTH TOKEN".data(using: String.Encoding.utf8)!
                responseHandler(authData, nil)
            }))
            
            let dictionaryToSend = ["iosenvironment":"https://clearws-la.oneclear.com","iosudid":"Android|e20c5a5cbd00d46e","referer":"dax.clearhub.tv","tenantid":"888","tenantids":"5001,5006,5010,5014,5021,5035,5040,5051","secretToken":"0776078023","userid":"95123"]

             struct dataToPass: Codable {
               var iosenvironment: String
                var iosudid: String
                var referer: String
                var tenantid: String
                var tenantids: String
                var secretToken: String
                var userid: String
            }

            let dog = dataToPass(iosenvironment: "https://clearws-la.oneclear.com", iosudid: "Android|e20c5a5cbd00d46e",referer:"dax.clearhub.tv",tenantid: "888", tenantids:  "5001,5006,5010,5014,5021,5035,5040,5051", secretToken: "0776078023", userid: "95123")
                            
            let authData = dog.convertToString!.data(using: String.Encoding.utf8)!
            responseHandler(authData,nil)

            self.present(alertController, animated: true, completion: nil)
            
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        self.server?.stop()
    }

    // MARK: - VoucherServerDelegate

    func voucherServer(_ server: VoucherServer, didUpdateAdvertising isAdvertising: Bool) {
        var text = "❌ Server Offline."
        if (isAdvertising) {
            text = "✅ Server Online."
        }
        self.serverStatusLabel.text = text
        self.connectionStatusLabel.isHidden = !isAdvertising
    }

    func voucherServer(_ server: VoucherServer, didUpdateConnectionToClient isConnectedToClient: Bool) {
        var text = "📡 Waiting for Connection..."
        if (isConnectedToClient) {
            text = "✅ Connected."
        }
        self.connectionStatusLabel.text = text
    }

}



extension Encodable {
    var convertToString: String? {
        let jsonEncoder = JSONEncoder()
        jsonEncoder.outputFormatting = .prettyPrinted
        do {
            let jsonData = try jsonEncoder.encode(self)
            return String(data: jsonData, encoding: .utf8)
        } catch {
            return nil
        }
    }
}
